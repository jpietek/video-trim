package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public interface FilterInterface {
	public String getId();
	public void setId(int id);
	public int getIn();
	public void setIn(int in);
	public int getOut();
	public void setOut(int out);
	public Directives toXml();
}
