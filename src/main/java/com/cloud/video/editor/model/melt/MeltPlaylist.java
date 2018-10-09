package com.cloud.video.editor.model.melt;

import java.util.ArrayList;
import java.util.List;

import org.xembly.Directives;

public class MeltPlaylist {

	private List<PlaylistEntry> entries = new ArrayList<PlaylistEntry>();
	
	private String id;
	
	public MeltPlaylist(String id) {
		this.id = id;
	}
	
	public MeltPlaylist(int id, List<PlaylistEntry> entries) {
		this.id = "playlist" + id;
		this.entries = entries;
	}
	
	public Directives toXml() {
		Directives playlist = new Directives()
				.xpath("/mlt")
				.add("playlist")
				.attr("id", id);

		for (PlaylistEntry e: entries) {
			playlist.append(
				e.toXml()
				.up());
		}
		
		return playlist;
	}

	public List<PlaylistEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<PlaylistEntry> entries) {
		this.entries = entries;
	}
	
	public void addEntry(PlaylistEntry entry) {
		this.entries.add(entry);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
