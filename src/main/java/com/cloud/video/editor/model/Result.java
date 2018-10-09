package com.cloud.video.editor.model;

public class Result {

	boolean success;
	String msg;
	Object result;
	int errCode;
	
	public Result(boolean success, String msg) {
		super();
		this.success = success;
		this.msg = msg;
	}
	
	public Result(boolean success, String msg, Object result) {
		super();
		this.success = success;
		this.msg = msg;
		this.result = result;
	}
	
	public Result(boolean success, String msg, Object result, int errCode) {
		super();
		this.success = success;
		this.msg = msg;
		this.result = result;
		this.errCode = errCode;
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	
}
