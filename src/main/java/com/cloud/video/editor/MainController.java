package com.cloud.video.editor;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.Filter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.video.editor.model.Compilation;
import com.cloud.video.editor.model.CompilationRepository;
import com.cloud.video.editor.model.CompilationRequest;
import com.cloud.video.editor.model.CompilationsRequest;
import com.cloud.video.editor.model.KeyframeSide;
import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.ShareCompilationRequest;
import com.cloud.video.editor.model.User;
import com.cloud.video.editor.model.UserRepository;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.utils.HttpUtils;
import com.cloud.video.editor.utils.Mp4Utils;
import com.cloud.video.editor.utils.StringUtils;

import lombok.extern.java.Log;

@SpringBootApplication
@EnableOAuth2Client
@RestController
@Log
public class MainController extends WebSecurityConfigurerAdapter {

	@Autowired
	private OAuth2ClientContext oauth2ClientContext;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CompilationRepository compilationRepository;

	private GoogleLogic googleLogic = new GoogleLogic(oauth2ClientContext);

	private DropboxLogic dropboxLogic = new DropboxLogic(
			"S00tGBw3cCkAAAAAAAAM6N6UvdHcsqGjkfI1jYGpeUS_ngU9XdFssa70QgK71aqw");

	private Executor videoExecutor = Executors.newFixedThreadPool(128);

	private static final String CHUNKS_DIR = "chunks";

	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean
	@ConfigurationProperties("google.client")
	public AuthorizationCodeResourceDetails google() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("google.resource")
	public ResourceServerProperties googleResource() {
		return new ResourceServerProperties();
	}

	private Filter ssoFilter() {
		OAuth2ClientAuthenticationProcessingFilter googleFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/google");
		OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);
		googleFilter.setRestTemplate(googleTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(googleResource().getUserInfoUri(),
				google().getClientId());
		tokenServices.setRestTemplate(googleTemplate);
		googleFilter.setTokenServices(tokenServices);

		return googleFilter;

	}

	@RequestMapping("/user")
	public Principal user(Principal principal) {
		log.info("user: " + principal);
		OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
		Authentication authentication = oAuth2Authentication.getUserAuthentication();

		final String email = "email";

		@SuppressWarnings("unchecked")
		Map<String, String> details = (Map<String, String>) authentication.getDetails();
		log.info("mail: " + details.get(email));
		log.info("gender: " + details.get("gender"));
		log.info("profile: " + details.get("profile"));
		log.info("picture: " + details.get("picture"));
		if (!userRepository.existsByEmail(details.get(email))) {
			User u = new User();
			u.setName(principal.getName());
			u.setEmail(details.get("email"));
			u.setGender(details.get("gender"));
			u.setProfile(details.get("profile"));
			u.setPicture(details.get("picture"));
			userRepository.save(u);
		}
		return principal;
	}

	@PostMapping(value = "/compilation/share")
	public Result shareCompilation(@RequestBody ShareCompilationRequest req) {
		Compilation comp = compilationRepository.findById(req.getCompilationId());
		User u = userRepository.getByEmail(req.getEmail());
		comp.getUsers().add(u);
		compilationRepository.save(comp);
		return new Result(true, "share ok");
	}

	@PostMapping(value = "/user/compilations/list/mail")
	public List<Compilation> getUserCompilations(@RequestBody CompilationsRequest req) {
		log.info("user mail: " + req.getMail());
		User u = userRepository.getByEmail(req.getMail());
		List<Compilation> comps = compilationRepository.findByUser(u);
		log.info("comps size: " + comps.size());
		comps.stream().forEach(c -> log.info(c.toString()));
		return comps;
	}

	@PostMapping("/google/list")
	public List<Video> listGoogle() {
		return googleLogic.listFiles();
	}

	@GetMapping(value = "/dropbox/list/thumbsize/{size}")
	public List<Video> listDropbox(@PathVariable("size") String size) {
		return dropboxLogic.listVideos(size);
	}

	@PostMapping(value = "/google/getDirectLink")
	public Video getGoogleUrl(@RequestBody Video video) {
		googleLogic.getDirectLink(video);
		return video;
	}

	@PostMapping(value = "/dropbox/getDirectLink")
	public Video getDropboxUrl(@RequestBody Video video) {
		Result updatedVideoRes = dropboxLogic.getDirectLink(video);
		if (!updatedVideoRes.isSuccess()) {
			log.info(updatedVideoRes.getMsg());
		}
		return video;
	}

	private Result saveCompilation(CompilationRequest compilationReq) {
		User user = userRepository.getByEmail(compilationReq.getUserMail());
		if (user == null) {
			return new Result(false, "user not found");
		}
		final Set<Video> videos = compilationReq.getVideos();
		final Compilation c = new Compilation();
		videos.stream().forEach(v -> v.setCompilation(c));

		c.getUsers().add(user);
		c.setVideos(compilationReq.getVideos());
		c.setDuration(compilationReq.getTotalDuration());
		c.setFps(25);
		c.setModified(new Date());
		c.setName(compilationReq.getName());

		compilationRepository.save(c);
		return new Result(true, "compilation saved");

	}

	@RequestMapping("/render")
	public Result render(@RequestBody CompilationRequest compilationReq) {
		Result saveRes = this.saveCompilation(compilationReq);
		if (!saveRes.isSuccess()) {
			return saveRes;
		}

		final String basepath = "/var/www/html/out/" + StringUtils.getRandomId();

		try {
			FileUtils.forceMkdir(new File(basepath));
			FileUtils.forceMkdir(new File(HttpUtils.buildURL(basepath, CHUNKS_DIR)));
		} catch (IOException e) {
			return new Result(false, "can't create video basedir");
		}
		
		final Set<Video> clips = compilationReq.getVideos();
		List<Result> clipRenderResults = clips.parallelStream().map(c -> {

			final String url = c.getDirectContentLink();
			final double in = c.cutInSeconds();
			final double out = c.cutOutSeconds();

			final String leftPath = HttpUtils.buildURL(basepath, CHUNKS_DIR, c.getSortId() + "-left-full.ts");
			final String rightPath = HttpUtils.buildURL(basepath, CHUNKS_DIR, c.getSortId() + "-right-full.ts");
			final String leftTrimmedPath = leftPath.replace("-full", "");
			final String rightTrimmedPath = rightPath.replace("-full", "");
			final String middlePath = HttpUtils.buildURL(basepath, CHUNKS_DIR, c.getSortId() + "-middle.ts");

			final CompletionStage<Double> inLeftFuture = Mp4Utils.getLeftKeyframe(in, url);
			final CompletionStage<Double> inRightFuture = Mp4Utils.getRightKeyframe(in, url);
			final CompletionStage<Double> outLeftFuture = Mp4Utils.getLeftKeyframe(out, url);
			final CompletionStage<Double> outRightFuture = Mp4Utils.getRightKeyframe(out, url);

			final CompletableFuture<Boolean> leftChunkResult = inLeftFuture
					.thenCombine(inRightFuture, (leftKeyframe, rightKeyframe) -> {
						Mp4Utils.extractKeyFramedSegment(leftKeyframe, (Double) rightKeyframe, url, leftPath, 0.15);

						final double trimLeftIn = in - leftKeyframe;
						final double segmentDuration = (Double) rightKeyframe - leftKeyframe;
						final double leftDuration = segmentDuration - trimLeftIn;

						return Mp4Utils.trimReencodeSegment(c, trimLeftIn, leftDuration, leftPath, leftTrimmedPath,
								KeyframeSide.LEFT);
					}).toCompletableFuture();

			final CompletableFuture<Boolean> rightChunkResult = outLeftFuture
					.thenCombine(outRightFuture, (leftKeyframe, rightKeyframe) -> {
						Mp4Utils.extractKeyFramedSegment(leftKeyframe, (Double) rightKeyframe, url, rightPath, 0.15);

						final double trimRightIn = 0;
						final double rightDuration = out - leftKeyframe;

						return Mp4Utils.trimReencodeSegment(c, trimRightIn, rightDuration, rightPath, rightTrimmedPath,
								KeyframeSide.RIGHT);
					}).toCompletableFuture();

			final CompletableFuture<Boolean> middleChunkResult = inRightFuture
					.thenCombine(outLeftFuture, (leftKeyframe, rightKeyframe) -> {
						if (leftKeyframe == (double) rightKeyframe) {
							return false;
						}

						final double gopLength = (Double) rightKeyframe - leftKeyframe;
						final double leftOffset = 0.07 * gopLength;

						return Mp4Utils.extractKeyFramedSegment(leftKeyframe, (Double) rightKeyframe, url, middlePath,
								leftOffset);
					}).toCompletableFuture();

			final List<String> chunksToConcat = new ArrayList<>();
			try {
			if (leftChunkResult.get()) {
				chunksToConcat.add(leftTrimmedPath);
			}
			if (middleChunkResult.get()) {
				chunksToConcat.add(middlePath);
			}
			if (rightChunkResult.get()) {
				chunksToConcat.add(rightTrimmedPath);
			}
			return Mp4Utils.concatProtocol(chunksToConcat, basepath + "/" + c.getSortId() + ".mp4");
			} catch(ExecutionException e) {
				return new Result(false, e.getMessage());
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return new Result(false, e.getMessage());
			}

		}).collect(Collectors.toList());

		Optional<Result> failed = clipRenderResults.stream().filter(res -> !res.isSuccess()).findFirst();

		if (failed.isPresent()) {
			return new Result(false, "one of the clip renders failed, " + failed.get().getMsg());
		}

		if (clips.size() == 1) {
			return clipRenderResults.get(0);
		}

		List<String> chunkPaths = IntStream.range(0, clips.size())
				.mapToObj(n -> HttpUtils.buildURL(basepath, n + ".mp4")).collect(Collectors.toList());

		log.info("chunk paths: " + chunkPaths);
		String mp4Out = basepath + "/out.mp4";
		return Mp4Utils.fileConcat(chunkPaths, mp4Out);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/logout**", "/webjars/**").permitAll()
				.anyRequest().authenticated().and().logout().logoutSuccessUrl("/").permitAll().and().csrf()
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
				.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}
}
