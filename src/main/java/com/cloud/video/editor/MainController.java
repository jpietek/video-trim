package com.cloud.video.editor;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.video.editor.model.Compilation;
import com.cloud.video.editor.model.CompilationRepository;
import com.cloud.video.editor.model.CompilationRequest;
import com.cloud.video.editor.model.CompilationsRequest;
import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.User;
import com.cloud.video.editor.model.UserRepository;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.probe.Probe;
import com.cloud.video.editor.utils.Mp4Utils;
import com.cloud.video.editor.utils.ProbeUtils;
import com.cloud.video.editor.utils.StringUtils;

@SpringBootApplication
@EnableOAuth2Client
@RestController
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
		System.out.println("user: " + principal);
		OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
		Authentication authentication = oAuth2Authentication.getUserAuthentication();
		@SuppressWarnings("unchecked")
		Map<String, String> details = (Map<String, String>) authentication.getDetails();
		System.out.println("mail: " + details.get("email"));
		System.out.println("gender: " + details.get("gender"));
		System.out.println("profile: " + details.get("profile"));
		System.out.println("picture: " + details.get("picture"));
		if (!userRepository.existsByEmail(details.get("email"))) {
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

	@RequestMapping(value = "/user/compilations/list/mail", method = RequestMethod.POST)
	public List<Compilation> getUserCompilations(@RequestBody CompilationsRequest req) {
		System.out.println("user mail: " + req.getMail());
		List<Compilation> comps = compilationRepository.findFirst10ByUserEmailOrderByModifiedDesc(req.getMail());
		System.out.println("comps size: " + comps.size());
		comps.stream().forEach(c -> System.out.println(c.toString()));
		return comps;
	}

	@RequestMapping("/google/list")
	public List<Video> listGoogle() {
		return googleLogic.listFiles();
	}

	@RequestMapping(value = "/dropbox/list/thumbsize/{size}", method = RequestMethod.GET)
	public List<Video> listDropbox(@PathVariable("size") String size) {
		return dropboxLogic.listVideos(size);
	}

	@RequestMapping(value = "/google/getDirectLink", method = RequestMethod.POST)
	public Video getGoogleUrl(@RequestBody Video v) {
		googleLogic.getDirectLink(v);
		return v;
	}

	@RequestMapping(value = "/dropbox/getDirectLink", method = RequestMethod.POST)
	public Video getDropboxUrl(@RequestBody Video v) {
		dropboxLogic.getDirectLink(v);
		return v;
	}

	@RequestMapping("/preview")
	public String preview() {
		return "dupa";
	}

	private Result saveCompilation(CompilationRequest compilationReq) {
		User user = userRepository.getByEmail(compilationReq.getUserMail());
		if (user == null) {
			return new Result(false, "user not found");
		}
		final Set<Video> videos = compilationReq.getVideos();
		final Compilation c = new Compilation();
		videos.stream().forEach(v -> v.setCompilation(c));

		c.setUser(user);
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

		String basepath = "/var/www/html/out/" + StringUtils.getRandomId();

		try {
			FileUtils.forceMkdir(new java.io.File(basepath));
		} catch (IOException e) {
			return new Result(false, "can't create tmp dir for redering");
		}

		final Set<Video> clips = compilationReq.getVideos();
		List<Result> clipRenderResults = clips.parallelStream().map(c -> {
			final String url = c.getDirectContentLink();
			final double in = c.cutInSeconds();
			final double out = c.cutOutSeconds();

			List<String> chunksToConcat = new ArrayList<>();
			CompletableFuture<Result> inResFuture = CompletableFuture.supplyAsync(() -> {
				return Mp4Utils.getIFramesNearTimecodeFast(in, url);
			});
			CompletableFuture<Result> outResFuture = CompletableFuture.supplyAsync(() -> {
				return Mp4Utils.getIFramesNearTimecodeFast(out, url);
			});
			
			CompletableFuture<Result> probeResFuture = CompletableFuture.supplyAsync(() -> {
				return ProbeUtils.probeVideo(url);
			});

			Result inRes = inResFuture.join();
			Result outRes = outResFuture.join();

			if (!inRes.isSuccess() || !outRes.isSuccess()) {
				System.out.println("extracting iframed tses failed: " + inRes.getMsg() + " " + outRes.getMsg());
				return new Result(false, "extracting iframed tses failed: " + inRes.getMsg() + " " + outRes.getMsg());
			}

			Pair<Double, Double> cutInIframeTs = (Pair<Double, Double>) inRes.getResult();
			Pair<Double, Double> cutOutIframeTs = (Pair<Double, Double>) outRes.getResult();
			Set<Double> keyframes = new HashSet<Double>();
			keyframes.add(cutInIframeTs.getLeft());
			keyframes.add(cutInIframeTs.getRight());
			keyframes.add(cutOutIframeTs.getLeft());
			keyframes.add(cutOutIframeTs.getRight());
			
			Result probeRes = probeResFuture.join();
			if(!probeRes.isSuccess()) {
				return new Result(false, "cant ffprobe video");
			}
			
			Probe ffprobeParams = (Probe) probeRes.getResult();
			
			if (keyframes.size() < 2) {
				return new Result(false, "failed to extract at least 2 keyframes");
			}
			
			if(keyframes.size() == 2) {
				System.out.println("only 2 keyframes found, reencode the whole segment");
				return Mp4Utils.reencodeSingleSegment(url, in, out, basepath + "/" + c.getSortId() + ".mkv");
			}

			String middlePath = basepath + "/chunks/" + c.getSortId() + "-middle.mkv";
			CompletableFuture<Result> middleFuture = null;
			if (keyframes.size() == 4) {
				middleFuture = CompletableFuture.supplyAsync(() -> {
					return Mp4Utils.extractKeyFramedSegment(cutInIframeTs.getRight(), cutOutIframeTs.getLeft(), url,
							middlePath, c.getFps());
				});
			}

			if (!inRes.isSuccess() || !outRes.isSuccess()) {
				System.out.println("extracting iframed tses failed: " + inRes.getMsg() + " " + outRes.getMsg());
				return new Result(false, "extracting iframed tses failed: " + inRes.getMsg() + " " + outRes.getMsg());
			}

			System.out.println(cutInIframeTs + " " + in);
			System.out.println(cutOutIframeTs + " " + out);
			if (cutInIframeTs.equals(cutOutIframeTs)) {
				Result trimRes = Mp4Utils.trimReencodeSegment(cutInIframeTs.getLeft(), cutInIframeTs.getRight(), url,
						c.getFps(), "left", basepath + "/chunks/" + c.getSortId() + ".mkv", ffprobeParams);
				return trimRes;
			}

			final String leftPath = basepath + "/chunks/" + c.getSortId() + "-left-full.mkv";
			final String rightPath = basepath + "/chunks/" + c.getSortId() + "-right-full.mkv";
			try {
				FileUtils.forceMkdir(new File(basepath + "/chunks/"));
			} catch (IOException e) {
				return new Result(false, "can't make temp chunk dir");
			}

			CompletableFuture<Result> extractLeftResFuture = CompletableFuture.supplyAsync(() -> {
				return Mp4Utils.extractKeyFramedSegment(cutInIframeTs.getLeft(), cutInIframeTs.getRight(), url, leftPath,
						c.getFps());
			});
			CompletableFuture<Result> extractRightResFuture = CompletableFuture.supplyAsync(() -> {
				return Mp4Utils.extractKeyFramedSegment(cutOutIframeTs.getLeft(), cutOutIframeTs.getRight(), url, rightPath,
						c.getFps());
			});

			Result extractLeftRes = extractLeftResFuture.join();
			Result extractRightRes = extractRightResFuture.join();
			if (!extractLeftRes.isSuccess() || !extractRightRes.isSuccess()) {
				System.out.println("extracting keyframed segments failed: " + extractLeftRes + " " + extractRightRes);
				return new Result(false,
						"extracting keyframed segments failed: " + extractLeftRes + " " + extractRightRes);
			}

			final String leftTrimmedPath = leftPath.replace("-full", "");
			final String rightTrimmedPath = rightPath.replace("-full", "");
			double trimLeftIn = in - cutInIframeTs.getLeft();
			double trimRightIn = 0;
			double segmentDuration = cutInIframeTs.getRight() - cutInIframeTs.getLeft();
			double leftDuration = segmentDuration - trimLeftIn;
			double rightDuration = out - cutOutIframeTs.getLeft();
			Result leftTrimRes = Mp4Utils.trimReencodeSegment(trimLeftIn, leftDuration, leftPath, c.getFps(), "left",
					leftTrimmedPath, ffprobeParams);
			Result rightTrimRes = Mp4Utils.trimReencodeSegment(trimRightIn, rightDuration, rightPath, c.getFps(),
					"right", rightTrimmedPath, ffprobeParams);

			if (!leftTrimRes.isSuccess() || !rightTrimRes.isSuccess()) {
				System.out.println("reencode trim failed: " + leftTrimRes + " " + rightTrimRes);
				return new Result(false, "reencode trim failed: " + leftTrimRes + " " + rightTrimRes);
			}

			chunksToConcat.add(leftTrimmedPath);

			if (!cutInIframeTs.getRight().equals(cutOutIframeTs.getLeft()) && keyframes.size() == 4) {
				Result middleChunkRes = middleFuture.join();
				if (!middleChunkRes.isSuccess()) {
					System.out.println("reencode middle trim failed: " + middleChunkRes);
					return new Result(false, "reencode middle trim failed: " + middleChunkRes);
				}
				chunksToConcat.add(middlePath);
			}
			chunksToConcat.add(rightTrimmedPath);
			return Mp4Utils.fileConcat(chunksToConcat, basepath + "/" + c.getSortId() + ".mkv");
		}).collect(Collectors.toList());

		Optional<Result> failed = clipRenderResults.stream().filter(res -> !res.isSuccess()).findFirst();

		if (failed.isPresent()) {
			return new Result(false, "one of the clip renders failed, " + failed.get().getMsg());
		}

		List<String> chunkPaths = IntStream.range(0, clips.size()).mapToObj(n -> basepath + "/" + n + ".mkv")
				.collect(Collectors.toList());

		System.out.println("chunk paths: " + chunkPaths);
		String mkvOut = basepath + "/out.mkv";
		String mp4Out = basepath + "/out.mp4";
		return Mp4Utils.fileConcat(chunkPaths, mkvOut);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/logout**", "/webjars/**").permitAll()
				.anyRequest().authenticated().and().logout().logoutSuccessUrl("/").permitAll().and().csrf()
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
				.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}
}
