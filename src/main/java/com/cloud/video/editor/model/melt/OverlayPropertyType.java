package com.cloud.video.editor.model.melt;

public enum OverlayPropertyType {
	TEXT("Text"),
	URL("Url"),
	NUMBER("Number"),
	ENUM("Enum"),
	TEXTAREA("Textarea");
	
	private final String type;
	
    private OverlayPropertyType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
