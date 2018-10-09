package com.cloud.video.editor.model.melt;

public enum TransitionType {
	
	LUMA ("luma"), 
	DIPTOWHITE ("diptowhite"), 
	DIPTOBLACK ("diptoblack"),
	WIPE ("wipe");

	private String type;

	TransitionType(String type) {
		this.type =  type;
	}

	public String getDipTransitionColor() {
		if(type.equalsIgnoreCase("diptowhite")) {
			return "white";
		} else if(type.equalsIgnoreCase("diptoblack")) {
			return "black";
		}
		return null;
	}
	
	public boolean needsProducer() {
		return type.equalsIgnoreCase("diptowhite") || type.equalsIgnoreCase("diptoblack")
				|| type.equalsIgnoreCase("wipe");
	}
	
	public boolean isDip() {
		return type.equalsIgnoreCase("diptowhite") || type.equalsIgnoreCase("diptoblack");
	}

}
