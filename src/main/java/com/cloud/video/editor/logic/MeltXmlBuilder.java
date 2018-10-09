package com.cloud.video.editor.logic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.VideoQuality;
import com.cloud.video.editor.model.melt.AffineFilter;
import com.cloud.video.editor.model.melt.AspectRatioTransform;
import com.cloud.video.editor.model.melt.AudioTrack;
import com.cloud.video.editor.model.melt.AudioTransition;
import com.cloud.video.editor.model.melt.BlankPlaylistEntry;
import com.cloud.video.editor.model.melt.BlendTransition;
import com.cloud.video.editor.model.melt.ColorProducer;
import com.cloud.video.editor.model.melt.FilterInterface;
import com.cloud.video.editor.model.melt.InOutPoint;
import com.cloud.video.editor.model.melt.LumaTransition;
import com.cloud.video.editor.model.melt.MeltPlaylist;
import com.cloud.video.editor.model.melt.MeltSession;
import com.cloud.video.editor.model.melt.MeltTransitionInterface;
import com.cloud.video.editor.model.melt.PlaylistEntry;
import com.cloud.video.editor.model.melt.Producer;
import com.cloud.video.editor.model.melt.ProducerEntry;
import com.cloud.video.editor.model.melt.ProducerInterface;
import com.cloud.video.editor.model.melt.SlowMotionProducer;
import com.cloud.video.editor.model.melt.Track;
import com.cloud.video.editor.model.melt.Tractor;
import com.cloud.video.editor.model.melt.Transition;
import com.cloud.video.editor.model.melt.TransitionType;
import com.cloud.video.editor.model.melt.VolumeFilter;
import com.cloud.video.editor.model.melt.Watermark;
import com.cloud.video.editor.model.melt.WatermarkFilter;
import com.cloud.video.editor.utils.MathUtils;

class AVProducer {
	Producer video;
	Producer audio;
}

public class MeltXmlBuilder {

	private SortedMap<Integer, AVProducer> clipsAVProducers = new TreeMap<Integer, AVProducer>();
	private SortedMap<Integer, Tractor> tractors = new TreeMap<Integer, Tractor>();

	Integer globalAudioProducerIndex;

	private VideoQuality quality;

	private Integer previewId;

	private List<Video> videoClips = new ArrayList<Video>();

	private Directives xmlDir = new Directives().add("mlt");

	private MeltSession meltSession = new MeltSession();

	private final static Logger LOGGER = Logger.getLogger(MeltXmlBuilder.class.getName());

	private final static int DEFAULT_FADE_OUT__FRAMES = 12;
	private final static int DEFAULT_FADE_IN_FRAMES = 25;

	private final static String watermarkBasePath = "/var/www/html/watermarks/";

	private final static String previewBasePath = "/var/www/html";

	public MeltXmlBuilder() {

	}

	public MeltXmlBuilder(ArrayList<Video> videoClips, VideoQuality quality, Integer previewId) {
		this.videoClips = videoClips;
		this.quality = quality;
		this.previewId = previewId;
	}

	private int getTotalDurationInFrames() {
		int totalDuration = 0;
		for (Video c : this.videoClips) {
			totalDuration += c.getFrameCount();
		}

		for (Transition t : this.meltSession.getTransitions()) {
			if (t.getTransitionType() == TransitionType.LUMA) {
				totalDuration -= t.getTransitionDuration();
			}
		}

		return totalDuration - 1;
	}

	private boolean hasTransitions() {
		List<Transition> transitions = this.meltSession.getTransitions();
		return (transitions != null && !transitions.isEmpty()) ? true : false;
	}

	private Transition getInTransition(int clip1Id) {
		if (this.hasTransitions()) {
			for (Transition t : this.meltSession.getTransitions()) {
				if (t.getClip1Id() == clip1Id) {
					return t;
				}
			}
		}
		return null;
	}

	private Transition getOutTransition(int clip2Id) {
		if (this.hasTransitions()) {
			for (Transition t : this.meltSession.getTransitions()) {
				if (t.getClip2Id() == clip2Id) {
					return t;
				}
			}
		}
		return null;
	}

	private Video getVideoClipBySortIndex(int sortIndex) {
		return this.videoClips.get(sortIndex);
	}

	private List<WatermarkFilter> getWatermarkFilters(int id) {
		List<WatermarkFilter> watermarks = new LinkedList<WatermarkFilter>();

		for (Watermark w : this.meltSession.getWatermarks()) {
			String watermarkLocalPath = this.watermarkBasePath
					+ FilenameUtils.getName(w.getWatermarkUrl());

			try {
				FileUtils.copyURLToFile(new URL(w.getWatermarkUrl()),
						new File(watermarkLocalPath));
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			watermarks.add(new WatermarkFilter(id, w.getIn(), w.getOut(), watermarkLocalPath,
					w.getxOffsetPrecentage(), w.getyOffsetPrecentage(),
					w.getxScalePercentage(), w.getyScalePercentage()));
			id++;
		}
		return watermarks;
	}

	private Set<Transition> getDipTransitions() {

		Set<Transition> d = new HashSet<Transition>();

		for (Transition t : this.meltSession.getTransitions()) {
			if (t.getTransitionType().needsProducer()) {
				d.add(t);
			}
		}

		return d;
	}

	public boolean hasAudioTracks() {
		return (!this.meltSession.getAudioTracks().isEmpty()) ? true : false;
	}

	private void addTractorFade(Video c, int transitionDuration, boolean fadeIn) {
		long in;
		long out;
		if (fadeIn) {
			in = 0;
			out = DEFAULT_FADE_OUT__FRAMES;
		} else {
			in = c.getFrameCount() - DEFAULT_FADE_IN_FRAMES;
			out = c.getFrameCount();
		}

		int startGain = (fadeIn) ? 0 : 1;
		int endGain = (fadeIn) ? 1 : 0;
		VolumeFilter fade = new VolumeFilter((int)in, (int)out, startGain, endGain);

		this.xmlDir.xpath("/mlt/tractor[@id='tractor" + c.getSortId() + "']")
				.append(fade.toXml());
	}

	private Producer addAudioProducer(String id, String audioUrl) {
		Producer audioProducer = new Producer(id, audioUrl);

		this.xmlDir.xpath("/mlt/producers").append(audioProducer.toXml());

		return audioProducer;
	}

	private List<Tractor> createTransitionTractors(List<Transition> transitions,
			HashMap<TransitionType, ProducerInterface> dipTransitionProducers) {

		List<Tractor> transitionTractors = new LinkedList<Tractor>();

		int i = this.tractors.lastKey() + 1;

		for (Transition t : transitions) {
			List<MeltTransitionInterface> mltTransitions = new LinkedList<MeltTransitionInterface>();
			int transitionDuration = t.getTransitionDuration();
			LumaTransition vt = new LumaTransition(0, transitionDuration);
			mltTransitions.add(vt);

			Video clip1 = this.getVideoClipBySortIndex(t.getClip1Id());
			Video clip2 = this.getVideoClipBySortIndex(t.getClip2Id());

			this.addTractorFade(clip1, t.getTransitionDuration(), false);
			this.addTractorFade(clip2, t.getTransitionDuration(), true);

			int producer1In = (int) (clip1.getFrameCount() - t.getTransitionDuration());
			int producer1Out = (int) clip2.getFrameCount();

			int producer2In = 0;
			int producer2Out = t.getTransitionDuration();

			if (t.getTransitionType().equals(TransitionType.LUMA)) {

				Track t1 = new Track(0, producer1In, producer1Out,
						"tractor" + t.getClip1Id());
				Track t2 = new Track(1, producer2In, producer2Out,
						"tractor" + t.getClip2Id());
				transitionTractors
						.add(new Tractor(i, Arrays.asList(t1, t2), null, mltTransitions));
				mltTransitions.add(new AudioTransition(1, transitionDuration));

			} else if (t.getTransitionType() == TransitionType.DIPTOBLACK
					|| t.getTransitionType() == TransitionType.DIPTOWHITE) {

				ProducerInterface transProducer = dipTransitionProducers
						.get(t.getTransitionType());
				Track prod1Track = new Track(0, producer1In, producer1Out,
						"tractor" + t.getClip1Id());
				Track deepTransitionTrackOut = new Track(1, producer2In, producer2Out,
						transProducer.getId());

				transitionTractors
						.add(new Tractor(i, Arrays.asList(prod1Track, deepTransitionTrackOut),
								null, mltTransitions));
				i++;
				Track deepTransitionTrackIn = new Track(0, t.getTransitionDuration(),
						transProducer.getId());
				Track prod2Track = new Track(0, t.getTransitionDuration(),
						"tractor" + t.getClip2Id());
				transitionTractors
						.add(new Tractor(i, Arrays.asList(deepTransitionTrackIn, prod2Track),
								null, mltTransitions));
			} else if (t.getTransitionType() == TransitionType.WIPE) {

				int inPoint = (int) (clip1.getFrameCount() - t.getTransitionDuration() / 2);

				int outPoint = t.getTransitionDuration() / 2;

				producer1In = inPoint > 0 ? inPoint : 0;
				producer1Out = (int) clip1.getFrameCount();
				producer2In = 0;
				producer2Out = (int) (outPoint < clip2.getFrameCount() ? outPoint
						: clip2.getFrameCount());

				MeltPlaylist wipePlaylist = new MeltPlaylist("wipePlaylist" + i);

				wipePlaylist.addEntry(new ProducerEntry(producer1In, producer1Out,
						"tractor" + t.getClip1Id()));
				wipePlaylist.addEntry(new ProducerEntry(producer2In, producer2Out,
						"tractor" + t.getClip2Id()));
				this.xmlDir.append(wipePlaylist.toXml());

				ProducerInterface wipeProducer = dipTransitionProducers
						.get(t.getTransitionType());

				Track clipsTrack = new Track(0, "wipePlaylist" + i);
				Track wipeTrack = new Track(1, wipeProducer.getId());

				transitionTractors.add(new Tractor(i, Arrays.asList(clipsTrack, wipeTrack),
						null, Arrays.asList(new BlendTransition(0, t.getTransitionDuration()),
								new AudioTransition(0))));
			}
			i++;
		}

		LOGGER.info("after transition tractors: " + transitionTractors.size());
		return transitionTractors;
	}

	public Result generateVideoProducers() {

		this.xmlDir.add("producers");

		for (Video c : this.videoClips) {
			String clipPath = c.getDirectContentLink();
			if (clipPath == null) {
				LOGGER.info("can't get full chunk of the clip " + c.getSortId());
				return new Result(false, "can't get full chunk of the clip");
			}

			int in = (int) (c.getCutIn() * c.getFrameCount());
			int out = (int) (c.getCutIn() * c.getFrameCount());

			Producer video = (c.getSpeed() != 1.0)
					? new SlowMotionProducer("video" + c.getSortId(), in, out, clipPath,
							c.getSpeed(), c.getFps())
					: new Producer("video" + c.getSortId(), clipPath, in, out);

			Producer videoCopy = (c.getSpeed() != 1.0)
					? new SlowMotionProducer("video" + c.getSortId() + "-1", in, out, clipPath,
							c.getSpeed(), c.getFps())
					: new Producer("video" + c.getSortId() + "-1", clipPath, in, out);

			AVProducer avp = new AVProducer();
			avp.video = video;

			this.clipsAVProducers.put(c.getSortId(), avp);

			xmlDir.append(video.toXml());
			xmlDir.append(videoCopy.toXml());
		}

		this.generateAudioProducers();
		xmlDir.up();

		return new Result(true, "video producers generated ok");
	}

	private Result generateAudioProducers() {
		// Producers for audio loops (per clip & global)
		Map<String, Producer> autoUrlsAndProducers = new HashMap<String, Producer>();
		int i = 0;

		for (Video c : this.videoClips) {
			LOGGER.info("audio source: " + c.getAudioSource());
			if (c.getAudioSource() != null && !c.getAudioSource().isEmpty()) {
				Producer audioProducer = autoUrlsAndProducers.get(c.getAudioSource());
				if (audioProducer == null) {
					audioProducer = new Producer("audio" + i, c.getAudioSource());
					autoUrlsAndProducers.put(c.getAudioSource(), audioProducer);
					audioProducer.setLoop(true);
					xmlDir.append(audioProducer.toXml());
				}

				AVProducer avp = this.clipsAVProducers.get(c.getSortId());
				avp.audio = audioProducer;
				i++;
			}
		}

		return new Result(true, "audio loops producers generated ok");
	}

	private AffineFilter generateAffineFilter(List<AspectRatioTransform> affines, Video c) {
		// double the last&first transform at the first/last keyframe
		if (affines.size() > 0) {
			AspectRatioTransform firstTransform = affines.get(0);
			firstTransform.setKeyframe(0);

			AspectRatioTransform lastTransform = affines.get(affines.size() - 1);
			AspectRatioTransform doubledLastKeyframeTransform = new AspectRatioTransform();
			doubledLastKeyframeTransform.setKeyframe(c.getFrameCount());
			doubledLastKeyframeTransform.setxPosition(lastTransform.getxPosition());
			doubledLastKeyframeTransform.setyPosition(lastTransform.getyPosition());
			affines.add(doubledLastKeyframeTransform);
		}

		int sourceHeight = quality.getHeight();
		int sourceWidth = quality.getWidth();

		int clippedHeight = quality.getHeight();
		int clippedWidth = MathUtils.nearestDivisibleByTwo(
				clippedHeight * ((double) this.meltSession.getAspectRatioNumerator()
						/ this.meltSession.getAspectRatioDenominator()));

		AffineFilter affineFilter = new AffineFilter(0, c.getAffineTransforms(),
				clippedWidth, clippedHeight, sourceWidth, sourceHeight);

		return affineFilter;
	}

	public Result generateVideoTractors() {
		for (Video c : videoClips) {
			AVProducer avp = this.clipsAVProducers.get(c.getSortId());
			if (avp == null) {
				return new Result(false, "producer is missing for clip " + c.getSortId());
			}

			Track track = new Track(0, avp.video.getId());
			List<Track> tracks = new ArrayList<Track>();
			tracks.add(track);

			if (c.getSpeed() != 1.0) {
				track.setHide("audio");
			}

			if (c.getAudioSource() != null) {
				track.setHide("audio");
				Track audioTrack = new Track(1, 0, (int) c.getFrameCount(), avp.audio.getId());
				tracks.add(audioTrack);
			}

			Set<AspectRatioTransform> affines = c.getAffineTransforms();

			AffineFilter affineFilter = null;
			LOGGER.info("affines: " + affines.size());
			if (affines != null && !affines.isEmpty()) {
				affineFilter = this.generateAffineFilter(new ArrayList<AspectRatioTransform>(affines), c);
			}

			List<FilterInterface> affineFilters = new ArrayList<FilterInterface>();
			if (affineFilter != null) {
				affineFilters.add(affineFilter);
			}
			Tractor videoTractor = new Tractor(c.getSortId(), tracks, affineFilters);

			tractors.put(c.getSortId(), videoTractor);
			xmlDir.append(videoTractor.toXml());
		}

		return new Result(true, "video tractors generated ok");
	}

	public Result generateTransitions() {
		// Transition producers&tractors
		HashMap<TransitionType, ProducerInterface> dipTransitionProducers = new HashMap<TransitionType, ProducerInterface>();

		if (this.hasTransitions()) {
			LOGGER.info("has transitions");
			for (Transition t : this.getDipTransitions()) {
				ProducerInterface p;

				if (t.getTransitionType() == TransitionType.WIPE) {
					p = new Producer(
							"producer" + Integer.valueOf(this.clipsAVProducers.size() + 1),
							t.getMediaPath());
					p.setType("framebuffer");
				} else {
					p = new ColorProducer(t.getTransitionType().getDipTransitionColor());
				}

				dipTransitionProducers.put(t.getTransitionType(), p);

				xmlDir.append(p.toXml());
			}

			List<Tractor> transitionTractors = this.createTransitionTractors(
					this.meltSession.getTransitions(), dipTransitionProducers);

			if (!tractors.isEmpty()) {
				for (Tractor t : transitionTractors) {
					LOGGER.info("appending transition tractor");
					tractors.put(t.getIndex(), t);
					xmlDir.append(t.toXml());
				}
			}
		}
		return new Result(true, "transitions generated ok");
	}

	public Result generatePlaylist() {
		int clipIndex = 0;
		int transitionIndex = this.videoClips.size();
		MeltPlaylist meltPlaylist = new MeltPlaylist("mainPlaylist");
		for (Video c : this.videoClips) {
			int inFrame = 0;
			Transition inTransition = this.getInTransition(c.getSortId() - 1);
			if (clipIndex != 0 && inTransition != null) {
				inFrame = (inTransition.getTransitionType() == TransitionType.WIPE)
						? inTransition.getTransitionDuration() / 2
						: inTransition.getTransitionDuration();
			}
			int outFrame = (int) c.getFrameCount();

			Transition outTransition = this.getOutTransition((int) (c.getFrameCount() + 1));
			if (outTransition != null) {
				outFrame -= (outTransition.getTransitionType() == TransitionType.LUMA)
						? outTransition.getTransitionDuration()
						: outTransition.getTransitionDuration() / 2;
			}

			if (c.getSpeed() == 1.0 && c.getAffineTransforms().isEmpty()) {
				String producer = (this.getOutTransition(c.getSortId()) != null)
						? "video" + clipIndex + "-1" : "video" + clipIndex;
				meltPlaylist.addEntry(new ProducerEntry(inFrame, outFrame, producer));
			} else {
				meltPlaylist
						.addEntry(new ProducerEntry(inFrame, outFrame, "tractor" + clipIndex));
			}

			if (outTransition != null) {
				int outFrame2 = outTransition.getTransitionDuration();
				meltPlaylist
						.addEntry(new ProducerEntry(0, outFrame2, "tractor" + transitionIndex));
				transitionIndex++;

				if (outTransition.getTransitionType().isDip()) {
					meltPlaylist.addEntry(
							new ProducerEntry(0, outFrame2, "tractor" + transitionIndex));
					transitionIndex++;
				}
			}
			clipIndex++;
		}
		LOGGER.info("append playlist: " + meltPlaylist.toXml());
		xmlDir.append(meltPlaylist.toXml());

		return new Result(true, "melt playlist generated ok");
	}

	public void seekFinalTractor(String finalPlaylist) {
		Track seekTrack = new Track(0, finalPlaylist);
		seekTrack.setIn(meltSession.getStartFrame());
		seekTrack.setOut(this.getTotalDurationInFrames() - 1);

		Tractor seekTractor = new Tractor(
				"tractor" + Integer.valueOf(tractors.lastKey() + 1),
				Arrays.asList(seekTrack));
		xmlDir.append(seekTractor.toXml());
	}

	public Result generateFilters(String playlist) {
		List<FilterInterface> filterList = new ArrayList<FilterInterface>();
		List<Track> trackList = new ArrayList<Track>();

		trackList.add(new Track(0, playlist));

		// filters for watermarks
		if (!this.meltSession.getWatermarks().isEmpty()) {
			filterList.addAll(this.getWatermarkFilters(filterList.size()));
		}

		if (!filterList.isEmpty()) {
			List<MeltTransitionInterface> transitionList = new LinkedList<MeltTransitionInterface>();
			Tractor filterTractor = new Tractor(tractors.lastKey() + 1, trackList,
					filterList, transitionList);
			tractors.put(tractors.lastKey() + 1, filterTractor);
			xmlDir.append(filterTractor.toXml());
			return new Result(true, "filters generated ok");
		}

		return new Result(false, "no filters generated");
	}

	public void generateVoiceoverPlaylist() {
		int lastOut = 0;
		MeltPlaylist voiceOverPlaylist = new MeltPlaylist("voiceoverPlaylist");

		int index = this.clipsAVProducers.lastKey()
				+ ((this.globalAudioProducerIndex != null) ? 1 : 0) + 1;

		List<AudioTrack> audioTracks = meltSession.getAudioTracks();
		Collections.sort(audioTracks);

		for (AudioTrack a : meltSession.getAudioTracks()) {
			int blankDuration = a.getIn() - lastOut;
			BlankPlaylistEntry blank = new BlankPlaylistEntry(blankDuration);

			Producer audioProducer = this.addAudioProducer("audio" + index,
					a.getAudioLocalPath());
			voiceOverPlaylist.addEntry(blank);

			PlaylistEntry audioEntry = new ProducerEntry(audioProducer.getId());
			voiceOverPlaylist.addEntry(audioEntry);
			lastOut = a.getOut();
			index++;
		}

		this.xmlDir.append(voiceOverPlaylist.toXml());
	}

	public List<FilterInterface> generateUserAudioFades() {

		double highGain = 1.0;
		double lowGain = (double) meltSession.getVolumeRatio() / 100;

		LOGGER.info("audio track size: " + meltSession.getAudioTracks().size());
		List<FilterInterface> volumeFilters = new ArrayList<FilterInterface>();
		for (AudioTrack a : meltSession.getAudioTracks()) {
			int fadeOffset = 10;
			int in1 = (a.getIn() - fadeOffset < 0) ? 0 : (a.getIn() - fadeOffset);
			int out1 = a.getIn() + fadeOffset;
			VolumeFilter fadeIn = new VolumeFilter(in1, out1, highGain, lowGain);
			volumeFilters.add(fadeIn);

			int in2 = a.getOut() - fadeOffset;
			int out2 = a.getOut() + fadeOffset;

			VolumeFilter constGain = new VolumeFilter(out1 + 1, in2 - 1, lowGain, lowGain);
			volumeFilters.add(constGain);

			VolumeFilter fadeOut = new VolumeFilter(in2, out2, lowGain, highGain);
			volumeFilters.add(fadeOut);
		}

		return volumeFilters;
	}

	public List<FilterInterface> generateFades(List<InOutPoint> inoutPoints,
			boolean highGainIntervals) {
		List<FilterInterface> filters = new ArrayList<FilterInterface>();
		int lastFrame = 0;

		double gain1 = (highGainIntervals) ? 0 : 1.0;
		double gain2 = (highGainIntervals) ? 1.0 : 0;

		if (inoutPoints != null && !inoutPoints.isEmpty()) {
			for (InOutPoint p : inoutPoints) {
				// fade in
				boolean fadein = false;
				if (p.getIn() - 2 > 0) {
					filters.add(new VolumeFilter(lastFrame, p.getIn() - 2, gain1, gain1));
					VolumeFilter inOffsetFilter = new VolumeFilter(Math.max(0, p.getIn() - 1),
							p.getIn(), gain1, gain2);
					filters.add(inOffsetFilter);
					fadein = true;
				}

				filters.add(new VolumeFilter(p.getIn() + (fadein ? 1 : 0),
						Math.max(0, p.getOut() - 1), gain2, gain2));
				if (highGainIntervals) {
					filters.addAll(this.generateUserAudioFades());
				}
				// fade out
				boolean fadeout = false;
				if (p.getOut() < this.getTotalDurationInFrames()) {
					VolumeFilter outOffsetFilter = new VolumeFilter(p.getOut(), p.getOut() + 1,
							gain2, gain1);
					filters.add(outOffsetFilter);
					fadeout = true;
				}
				lastFrame = p.getOut() + (fadeout ? 1 : 0);
			}
		}
		if (lastFrame != 0) {
			filters.add(new VolumeFilter(lastFrame + 1, this.getTotalDurationInFrames(),
					gain1, gain1));
		}
		return filters;
	}

	public void generateFinalMix() {

		List<Track> tracks = new ArrayList<Track>();
		tracks.add(new Track(0, 0, this.getTotalDurationInFrames(), "voiceoverPlaylist"));
		tracks.add(new Track(1, 0, this.getTotalDurationInFrames(), "fadedVideoPlaylist"));

		int tractorIndex = tractors.lastKey() + 1;
		Tractor mix = new Tractor("tractor" + Integer.valueOf(tractorIndex), tracks,
				Arrays.asList(new AudioTransition(0)));
		tractors.put(tractorIndex, mix);

		xmlDir.append(mix.toXml());
	}

	public Result saveFile() {
		Result producersResult = this.generateVideoProducers();

		if (!producersResult.isSuccess()) {
			return producersResult;
		}

		this.generateVideoTractors();

		this.generateTransitions();

		String finalPlaylist = "mainPlaylist";
		this.generatePlaylist();

		if (this.hasAudioTracks()) {
			this.generateVoiceoverPlaylist();
		}

		if (this.generateFilters(finalPlaylist).isSuccess()) {
			finalPlaylist = "tractor" + tractors.lastKey();
		}

		this.seekFinalTractor(finalPlaylist);

		try {
			Document root = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
			new Xembler(this.xmlDir).apply(root);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = null;
			try {
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				return new Result(false, "xml generation failed, cannot create transformer");
			}
			DOMSource source = new DOMSource(root);
			try {
				FileUtils.forceMkdir(new File(previewBasePath + "/" + this.previewId));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String outputPath = previewBasePath + "/" + this.previewId + "/mltXml" + "-"
					+ this.quality.getName() + ".xml";
			LOGGER.info("saving xml to: " + outputPath);
			StreamResult result = new StreamResult(new File(outputPath));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				new Result(false, "xml generation failed");
			}
			return new Result(true, "xml generated", outputPath);
		} catch (ParserConfigurationException | ImpossibleModificationException e2) {
			e2.printStackTrace();
			return new Result(false, "xml generation failed");
		}
	}
}
