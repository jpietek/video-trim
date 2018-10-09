package com.cloud.video.editor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.stream.Stream;

public class SysUtils {

	public static Stream<String> getResultStream(String  cmd) {
		System.out.print("exec cmd: " + cmd);
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
			pb.inheritIO();
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.PIPE);
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			Stream<String> out = reader.lines();
			return out;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean getExitCode(String cmd) {
		System.out.print("exec cmd: " + cmd);
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		try {
			Process p = pb.start();
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.PIPE);
			p.waitFor();
			return p.exitValue() > 0 ? false : true;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}
}