package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public interface ProducerInterface {

	String getVideoPath();
	String getId();
	Integer getIn();
	Integer getOut();
	boolean isLoop();
	Double getSpeed();
	Directives toXml();
	String getType();
	void setType(String type);
}
