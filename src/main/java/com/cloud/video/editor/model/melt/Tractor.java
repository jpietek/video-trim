package com.cloud.video.editor.model.melt;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.xembly.Directives;

public class Tractor {

	private String id;
	private int index;
	private List<Track> tracks = new ArrayList<Track>();
	private List<FilterInterface> filters = new ArrayList<FilterInterface>();
	private List<MeltTransitionInterface> transitions;
	
	public Tractor(int id, List<Track> tracks, List<FilterInterface> filters, 
			List<MeltTransitionInterface> transitions) {
		this.id = "tractor" + id;
		this.index = id;
		this.tracks = tracks;
		this.filters = filters;
		this.transitions = transitions;
	}
	
	public Tractor(int id, List<Track> tracks, List<FilterInterface> filters) {
		this.id = "tractor" + id;
		this.index = id;
		this.tracks = tracks;
		this.filters = filters;
	}
	
	public Tractor(String id, List<Track> tracks, List<MeltTransitionInterface> transitions) {
		this.id = id;
		this.tracks = tracks;
		this.transitions = transitions;
	}
	
	public Tractor(String id, List<Track> tracks, List<MeltTransitionInterface> transitions, 
			List<FilterInterface> filters) {
		this.id = id;
		this.tracks = tracks;
		this.transitions = transitions;
		this.filters = filters;
	}
	
	public Tractor(String id, List<Track> tracks) {
		this.id = id;
		this.tracks = tracks;
	}


	public void addTrack(Track t) {
		this.tracks.add(t);
	}

	public void addFilter(FilterInterface f) {
		this.filters.add(f);
	}

	public void addFilters(List<FilterInterface> fl) {
		this.filters.addAll(fl);
	}

	
	public void addTransition(MeltTransitionInterface t) {
		this.transitions.add(t);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	public List<FilterInterface> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterInterface> filters) {
		this.filters = filters;
	}

	public List<MeltTransitionInterface> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<MeltTransitionInterface> transitions) {
		this.transitions = transitions;
	}

	public Directives toXml() {
		Directives tractor = null;
		tractor = new Directives()
				.xpath("/mlt")
				.add("tractor")
				.attr("id", id);

		if(this.tracks != null && !this.tracks.isEmpty()) {
			for(Track tr: this.tracks) {
				tractor
				.xpath("/mlt/tractor[@id='" + this.id + "']")
				.append(tr.toXml());
			}
		}
		
		if(this.transitions != null && !this.transitions.isEmpty()) {
			for (MeltTransitionInterface t: this.transitions) {
				tractor
				.xpath("/mlt/tractor[@id='" + this.id + "']")
				.append(t.toXml());
			}
		}
		if(this.filters != null && !this.filters.isEmpty()) {
			for(FilterInterface f: this.filters) {
				tractor
				.xpath("/mlt/tractor[@id='" + this.id + "']")
				.append(f.toXml());
			}
		}
		return tractor;
	}
}
