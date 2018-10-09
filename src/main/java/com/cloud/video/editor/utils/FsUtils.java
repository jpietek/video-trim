package com.cloud.video.editor.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FsUtils {
	
	public static List<byte[]> splitFile(int chunkSize, File file) {

		byte[] fileInBytes = null;
		try {
			fileInBytes = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
		}

		List<byte[]> videoChunks = new ArrayList<byte[]>();
		int len = fileInBytes.length;

		for (int i = 0; i < len - chunkSize + 1; i += chunkSize)
			videoChunks.add(Arrays.copyOfRange(fileInBytes, i, i + chunkSize));

		if (len % chunkSize != 0)
			videoChunks.add(Arrays.copyOfRange(fileInBytes, len - len % chunkSize, len));

		return videoChunks;
	}
	
	public static boolean isFilenameValid(String file) {
	    File f = new File(file);
	    try {
	       f.getCanonicalPath();
	       return true;
	    }
	    catch (IOException e) {
	       return false;
	    }
	}
}