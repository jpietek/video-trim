package com.cloud.video.editor.model.melt;

import java.util.List;

public class OverlayProperty {

	private String name;
	private String description;

	private OverlayPropertyType type;

	private int maxLength;

	private List<OverlayOption> options;

	public OverlayProperty() {
		super();
	}

	public OverlayProperty(String name, String description,
			OverlayPropertyType type, String value, int maxLength,
			List<OverlayOption> options) {
		super();
		this.name = name;
		this.description = description;
		this.type = type;
		this.maxLength = maxLength;
		this.options = options;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public OverlayPropertyType getType() {
		return type;
	}

	public void setType(OverlayPropertyType type) {
		this.type = type;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public List<OverlayOption> getOptions() {
		return options;
	}

	public void setOptions(List<OverlayOption> options) {
		this.options = options;
	}

}
