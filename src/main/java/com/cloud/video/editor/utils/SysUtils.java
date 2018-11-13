package com.cloud.video.editor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;

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
		NuProcessBuilder pb = new NuProcessBuilder(cmd.split(" "));
		try {
			NuProcess p = pb.start();
			int exitCode = p.waitFor(30, TimeUnit.SECONDS);
			return exitCode <= 0;
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