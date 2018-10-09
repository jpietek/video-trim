package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public interface MeltTransitionInterface {
	public String getId();
	public void setId(String id);
	public Integer getLength();
	public void setLength(int length);
	public Directives toXml();
}
