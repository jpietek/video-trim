package com.cloud.video.editor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.cloud.video.editor.model.Result;

public class SystemTools {

	private final static Logger LOGGER = Logger.getLogger(SystemTools.class.getName());

	public static int getUnixPID(Process process) {
		System.out.println(process.getClass().getName());
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			Class<?> cl = process.getClass();
			try {
				Field field = cl.getDeclaredField("pid");
				field.setAccessible(true);
				Object pidObject = field.get(process);
				return (int) pidObject;
			} catch (Exception e) {
				LOGGER.info("can't get process pid, not unix process");
			}
		}
		return -1;
	}

	public static Result killUnixProcess(Process process, int signal) {
		int pid = getUnixPID(process);

		if (pid == -1) {
			return new Result(false, "can't get ffmpeg process pid");
		}

		LOGGER.info("ffmpeg pid to kill: " + pid);

		try {
			Runtime.getRuntime().exec("kill -" + signal + " " + pid).waitFor();
		} catch (InterruptedException | IOException e) {
			LOGGER.info("ffmpeg kill exception");
			return new Result(false, "can't kill ffmpeg process with pid: " + pid);
		}

		LOGGER.info("ffmpeg kill ok");
		return new Result(true, "process killed");
	}

	public static boolean executeCommand(String cmd) {

		String[] commands = { "/bin/sh", "-c", cmd };

		Process proc;
		try {
			proc = Runtime.getRuntime().exec(commands);
			proc.waitFor();
			if (proc.exitValue() == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
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

		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}

}
