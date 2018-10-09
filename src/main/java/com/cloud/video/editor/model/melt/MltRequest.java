package com.cloud.video.editor.model.melt;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.apache.commons.net.telnet.TelnetClient;

import com.cloud.video.editor.model.Result;

public class MltRequest {	
	private TelnetClient tc;
	private InputStream in;
	private PrintStream out;

	protected static Logger LOGGER = Logger.getLogger(MltRequest.class.getName());

	private final static Pattern statPattern = Pattern.compile("^202\\sOK\\r\\n(.*)\\s\\d\\s\\d+\\s0$",
			Pattern.MULTILINE|Pattern.DOTALL);


	public MltRequest(MltUnit mu) {
		this.tc = new TelnetClient();
		try {
			this.tc.connect("localhost", mu.getMltPort());
			this.in = this.tc.getInputStream(); 
			this.out = new PrintStream(this.tc.getOutputStream());
			this.readUntil("100 VTR Ready"); 
		} catch (Exception e) {
			LOGGER.info("mlt req interrupted");
		}
	}

	@PreDestroy
	public void kill() {
		this.close();
	}

	public void close() {
		try {
			this.tc.disconnect();
		} catch(IOException e) {
			LOGGER.info("can't close req");
		}
	}

	public Result stats() {
		this.write("usta u0");
		Result getStatsRes = this.readStats();
		if(!getStatsRes.isSuccess()) {
			return getStatsRes;
		}

		String statsString = (String) getStatsRes.getResult();
		String[] statsFields = statsString.split(" ");
		
		if(statsFields.length < 9) {
			return new Result(false, "invalid stats result, play ended?");
		}

		String curFrame = statsFields[4];
		String duration = statsFields[8];

		PreviewStats stats = new PreviewStats(curFrame, duration);
		return new Result(true, "stats fetched from melt, ok", stats);
	}

	public void cmd(String cmd) throws MltCallException {
		this.write(cmd);
		try {
			String res = this.readUntil("OK");
			if(res == null) {
				throw new MltCallException("mlt cmd:  " 
						+ cmd + " failed");
			}
		} catch (IOException e) {
			throw new MltCallException("mlt cmd:  " + cmd + " failed, io exception");
		}
	}

	private void write(String value) { 
		this.out.println(value); 
		this.out.flush(); 
	} 

	private Result readStats() {
		char lastChar = '0'; 
		StringBuilder sb = new StringBuilder(); 
		int c; 
		try {
			while((c = this.in.read()) != -1) {
				char ch = (char) c; 
				sb.append(ch); 
				if(ch == lastChar) { 
					String str = sb.toString();
					Matcher m = statPattern.matcher(str);
					if(m.find()) { 
						return new Result(true, "stat pattern found", m.group());
					} 
				} 
			}
		} catch (IOException e) {
			LOGGER.info("stat pattern io error");
			return new Result(false, "reading telnet i/o failed"); 
		}
		return new Result(false, "stat pattern not found"); 
	} 

	private String readUntil(String pattern) throws IOException { 
		char lastChar = pattern.charAt(pattern.length() - 1); 
		StringBuilder sb = new StringBuilder(); 
		int c; 

		while((c = this.in.read()) != -1) { 
			char ch = (char) c; 
			sb.append(ch); 
			if(ch == lastChar) { 
				String str = sb.toString(); 

				if(str.contains(pattern)) { 
					return str.substring(0, str.length() - pattern.length()); 
				} 
			} 
		}
		return null; 
	} 
}

