package com.cloud.video.editor.model.melt;

import java.util.ArrayList;
import java.util.List;

import org.xembly.Directives;

public class ProducerEntry extends PlaylistEntry {

	private Integer in;
	private Integer out;
	private String entryId;
	
	private List<FilterInterface> filters = new ArrayList<FilterInterface>();
	
	public ProducerEntry(int in, int out, String entryId) {
		this.in = Math.max(0, in);
		this.out = Math.max(0, out);
		this.entryId = entryId; 
	}
	
	public ProducerEntry(String entryId) {
		this.entryId = entryId; 
	}
	
	public Directives toXml() {
		Directives entry = new Directives()
				.add("entry");
		
		if(this.in != null) {
			entry.attr("in", in);
		}
		
		if(this.out != null) {
			entry.attr("out", out);
		}
			
		entry.attr("producer", this.entryId);
		
		for (FilterInterface f: filters) {
			entry.append(
				f.toXml()
				.up());
		}
		
		return entry;
	}
	
	public int getIn() {
		return in;
	}
	public void setIn(int in) {
		this.in = in;
	}
	public int getOut() {
		return out;
	}
	public void setOut(int out) {
		this.out = out;
	}
	public String getEntryId() {
		return entryId;
	}
	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}
	public List<FilterInterface> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterInterface> filters) {
		this.filters = filters;
	}
	
	public void addFilter(FilterInterface filter) {
		this.filters.add(filter);
	}
	
	public void addFilters(List<FilterInterface> filters) {
		this.filters.addAll(filters);
	}
}
