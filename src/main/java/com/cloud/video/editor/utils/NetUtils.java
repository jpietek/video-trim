package com.cloud.video.editor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Utils doing the TCP/HTTP calls
 */
public class NetUtils {

	private final static Logger LOGGER = Logger.getLogger(NetUtils.class
			.getName());

	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This is used to check it the other stm node is active It's like telnet
	 * host port returning bool
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static boolean serverListening(String host, int port) {
		Socket s = null;
		try {
			s = new Socket(host, port);
			return true;
		} catch (Exception e) {
			LOGGER.info("serverListening: <" + host + ":" + port + "> : "
					+ e.getMessage());
			return false;
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (Exception e) {
				}
		}
	}

	public static boolean pingUrl(String url, int timeoutInSeconds) {
		url = url.replaceFirst("^https", "http"); // Otherwise an exception may
		// be thrown on invalid SSL
		// certificates.

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url)
					.openConnection();
			connection.setConnectTimeout(timeoutInSeconds * 1000);
			connection.setReadTimeout(timeoutInSeconds * 1000);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		} catch (IOException exception) {
			return false;
		}
	}

	public static String httpGet(String url, String authString) {
		HttpGet get = new HttpGet(url);
		if (authString != null) {
			get.addHeader("Authorization", "Basic " + authString);
		}
		HttpClient client = HttpClientBuilder.create().build();
		try {
			org.apache.http.HttpResponse resp = client.execute(get);
			HttpEntity entity = resp.getEntity();
			String responseString = EntityUtils.toString(entity);
			LOGGER.info("archive response: " + responseString);
			return responseString.replace("\\r\\n", "");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String httpPost(String url, String json, int timeoutInSeconds) {
		HttpPost post = new HttpPost(url);
		post.addHeader("content-type", "application/json");
		try {
			post.setEntity(new StringEntity(json));
		} catch (UnsupportedEncodingException e1) {

		}

		HttpClient client = HttpClientBuilder.create().build();
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeoutInSeconds * 1000)
				.setConnectTimeout(timeoutInSeconds * 1000)
				.setSocketTimeout(timeoutInSeconds * 1000).build();
		org.apache.http.HttpResponse stmResponse = null;
		post.setConfig(requestConfig);
		try {
			stmResponse = client.execute(post);
			HttpEntity entity = stmResponse.getEntity();
			String responseString = EntityUtils.toString(entity);
			LOGGER.info("archive response: " + responseString);
			return responseString.replace("\\r\\n", "");
		} catch (Exception e) {
			return null;
		}
	}

	public static String getStmHostname() {
		String hostname = NetUtils.getHostname();
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					whatismyip.openStream()));
			String ip = in.readLine();
			String amazonDomain = ip.replaceAll("\\.", "-") + ".tellyo.com";
			return amazonDomain;
		} catch (Exception e) {

		}
		return hostname;
	}
}
