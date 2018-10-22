package com.cloud.video.editor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import lombok.extern.java.Log;

@Log
public class SystemTools {

	private SystemTools() {

	}

	public static boolean executeCommand(String cmd) {

		String[] commands = { "/bin/sh", "-c", cmd };

		Process proc;
		try {
			proc = Runtime.getRuntime().exec(commands);
			proc.waitFor();
			return proc.exitValue() == 0;
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return false;
	}

	public static String getCommandReturnValue(String cmd) {
		String[] commands = { "/bin/sh", "-c", cmd };

		Process proc;
		try {
			proc = Runtime.getRuntime().exec(commands);
			proc.waitFor();
			if (proc.exitValue() == 0) {
				BufferedReader out = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				return out.readLine();
			} else {
				return "";
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return "";
	}

	public static int executeMeltJob(final String commandLine, final int timeoutInseconds)
			throws IOException, InterruptedException, TimeoutException {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(commandLine);
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join((long) timeoutInseconds * 1000);
			if (worker.exit != null)
				return worker.exit;
			else
				throw new TimeoutException();
		} catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	private static class Worker extends Thread {
		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
