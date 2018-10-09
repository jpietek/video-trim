package com.cloud.video.editor.model.melt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverlayParams implements Comparable<OverlayParams> {

	private String id;
	
	private String name;
	private String description;
	private List<OverlayProperty> properties = new ArrayList<OverlayProperty>();
	private String thumbnailUrl;
	
	private OverlayType type;
	private int defaultDuration;
	
	private int sortIndex;
	
	private Map<String, String> internalProperties = new HashMap<String, String>();
	
	public OverlayParams() {
		super();
	}
	public OverlayParams(String name, String id, String description, List<OverlayProperty> properties, String thumbnailUrl,
			OverlayType type, int defaultDuration, Map<String, String> internalProperties) {
		super();
		this.name = name;
		this.id = id;
		this.description = description;
		this.properties = properties;
		this.thumbnailUrl = thumbnailUrl;
		this.type = type;
		this.defaultDuration = defaultDuration;
		this.internalProperties = internalProperties;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<OverlayProperty> getProperties() {
		return properties;
	}
	public void setProperties(List<OverlayProperty> properties) {
		this.properties = properties;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public OverlayType getType() {
		return type;
	}
	public void setType(OverlayType type) {
		this.type = type;
	}
	public int getDefaultDuration() {
		return defaultDuration;
	}
	public void setDefaultDuration(int defaultDuration) {
		this.defaultDuration = defaultDuration;
	}
	
	public int getSortIndex() {
		return sortIndex;
	}
	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}
	
	public Map<String, String> getInternalProperties() {
		return internalProperties;
	}
	public void setInternalProperties(Map<String, String> internalProperties) {
		this.internalProperties = internalProperties;
	}
	@Override
	public int compareTo(OverlayParams o) {
		return o.getSortIndex() - this.sortIndex;
	}
}
