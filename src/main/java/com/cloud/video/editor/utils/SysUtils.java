package com.cloud.video.editor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.logging.Level;
import java.util.stream.Stream;

import lombok.extern.java.Log;

@Log
public class SysUtils {

	private SysUtils() {

	}

	public static Stream<String> getResultStream(String cmd) {
		log.info(cmd);
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
			pb.inheritIO();
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.PIPE);
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			return reader.lines();
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

	public static boolean getExitCode(String cmd) {
		log.info("exec cmd: " + cmd);
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		try {
			Process p = pb.start();
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.PIPE);
			p.waitFor();
			return p.exitValue() <= 0;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	public static boolean getRuntimeExitCode(String cmd) {
		log.info("exec cmd: " + cmd);
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			return p.exitValue() <= 0;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}
}