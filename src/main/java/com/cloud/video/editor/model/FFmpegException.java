package com.cloud.video.editor.model;

public class FFmpegException extends RuntimeException {

	private static final long serialVersionUID = 6328885212491408501L;
	private final String msg;

	public FFmpegException(String msg) {
		super(msg);
		this.msg = msg;
	}

}
