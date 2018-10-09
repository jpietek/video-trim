package com.cloud.video.editor.utils;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.cloud.video.editor.model.Result;

public class HttpUtils {

	private final static Logger LOGGER = Logger.getLogger(HttpUtils.class
			.getName());

	public static Result post(String url, String json, int timeoutInSeconds) {
		HttpPost post = new HttpPost(url);
		post.addHeader("content-type", "application/json");
		try {
			post.setEntity(new StringEntity(json));
		} catch (UnsupportedEncodingException e1) {

		}
		return call(post, timeoutInSeconds);
	}

	public static Result get(String url, int timeoutInSeconds) {
		HttpGet get = new HttpGet(url);
		return call(get, timeoutInSeconds);
	}

	private static Result call(HttpRequestBase request,
			int timeoutInSeconds) {
		HttpClient client = HttpClientBuilder.create().build();
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeoutInSeconds * 1000)
				.setConnectTimeout(timeoutInSeconds * 1000)
				.setSocketTimeout(timeoutInSeconds * 1000).build();
		org.apache.http.HttpResponse stmResponse = null;
		request.setConfig(requestConfig);
		try {
			stmResponse = client.execute(request);
			HttpEntity entity = stmResponse.getEntity();
			String responseString = EntityUtils.toString(entity).replace(
					"\\r\\n", "");
			LOGGER.info("http post response: " + responseString);
			if (stmResponse.getStatusLine().getStatusCode() == 200) {
				return new Result(true, responseString);
			} else {
				return new Result(false, "http call failed: "
						+ responseString);
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			LOGGER.severe("call: " + e.getMessage());
			return new Result(false, "timeout");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.severe("call: " + e.getMessage());
			return new Result(false, "HTTP call error : " + e.getMessage());
		}
	}

	public static String buildURL(String... parts) {
		StringBuilder bld = new StringBuilder();
		boolean first = true;
		for (String s : parts) {
			if (!first) {
				bld.append("/");
			}
			bld.append(s);
			first = false;
		}
		return bld.toString().replaceAll("(?<!:)//+", "/");
	}
}
