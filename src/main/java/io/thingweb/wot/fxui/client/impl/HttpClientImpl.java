package io.thingweb.wot.fxui.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import io.thingweb.wot.fxui.client.Callback;
import io.thingweb.wot.fxui.client.Content;
import io.thingweb.wot.fxui.client.MediaType;
import io.thingweb.wot.fxui.client.UnsupportedException;

public class HttpClientImpl extends AbstractClientImpl {

	private final static Logger LOGGER = Logger.getLogger(HttpClientImpl.class.getName());

	private static final int NTHREDS = 5;
	private static final ExecutorService executorService = Executors.newFixedThreadPool(NTHREDS);

	public HttpClientImpl() {
		super();
	}

	public void put(String propertyName, URI uri, Content propertyValue, Callback callback) throws UnsupportedException {
		put(propertyName, uri, propertyValue, callback, null);
	}

	public void put(String propertyName, URI uri, Content propertyValue, Callback callback, String securityAsToken) throws UnsupportedException {
		try {
			CallbackPutActionTask cgt = new CallbackPutActionTask(propertyName, uri, propertyValue, callback, false, securityAsToken);
			executorService.submit(cgt);
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			callback.onPutError(propertyName, e.getMessage());
		}
	}

	public void get(String propertyName, URI uri, Callback callback) throws UnsupportedException {
		get(propertyName, uri, callback, null);
	}

	public void get(String propertyName, URI uri, Callback callback, String securityAsToken) throws UnsupportedException {
		try {
			CallbackGetTask cgt = new CallbackGetTask(propertyName, uri, callback, securityAsToken);
			executorService.submit(cgt);
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			callback.onGetError(propertyName);
		}
	}

	public void observe(String propertyName, URI uri, Callback callback) throws UnsupportedException {
		observe(propertyName, uri, callback, null);
	}

	public void observe(String propertyName, URI uri, Callback callback, String securityAsToken) throws UnsupportedException {
		callback.onObserveError(propertyName);
		// throw new UnsupportedException("Not implemented yet");
	}

	public void observeRelease(String propertyName) throws UnsupportedException {
		throw new UnsupportedException("Not implemented yet");
	}

	public void action(String actionName, URI uri, Content actionValue, Callback callback) throws UnsupportedException {
		action(actionName, uri, actionValue, callback, null);
	}

	public void action(String actionName, URI uri, Content actionValue, Callback callback, String securityAsToken) throws UnsupportedException {

		try {
			CallbackPutActionTask cgt = new CallbackPutActionTask(actionName, uri, actionValue, callback, true, securityAsToken);
			executorService.submit(cgt);
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			callback.onActionError(actionName);
		}
	}

	class CallbackGetTask implements Runnable {
		private final String propertyName;
		private final URI uri;
		private final Callback callback;
		private final String securityAsToken;

		CallbackGetTask(String propertyName, URI uri, Callback callback) {
			this(propertyName, uri, callback, null);
		}

		CallbackGetTask(String propertyName, URI uri, Callback callback, String securityAsToken) {
			this.propertyName = propertyName;
			this.uri = uri;
			this.callback = callback;
			this.securityAsToken = securityAsToken;
		}

		protected void error(Exception e) {
			LOGGER.warning(e.getMessage());
			callback.onGetError(propertyName);
		}

		public void run() {
			try {
				URL url = uri.toURL();
				HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
				httpCon.setRequestMethod("GET");
				if(securityAsToken != null) {
					httpCon.setRequestProperty("Authorization", "Bearer " + securityAsToken);
				}

				try(InputStream is = httpCon.getInputStream()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int b;
					while ((b = is.read()) != -1) {
						baos.write(b);
					}

					String contentType = httpCon.getHeaderField("content-type");
					MediaType mediaType = MediaType.getMediaType(contentType);

					int responseCode = httpCon.getResponseCode();

					httpCon.disconnect();

					Content c = new Content(baos.toByteArray(), mediaType);

					if (responseCode == 200) {
						callback.onGet(propertyName, c);
					} else {
						// error
						error(new RuntimeException("ResponseCode==" + responseCode));
					}
				}
			} catch (Exception e) {
				error(e);
			}
		}
	}

	class CallbackPutActionTask implements Runnable {
		private final String name;
		private final URI uri;
		private final Callback callback;
		private final Content propertyValue;
		private final boolean isAction;
		private final String securityAsToken;

		CallbackPutActionTask(String name, URI uri, Content propertyValue, Callback callback, boolean isAction) {
			this(name, uri, propertyValue, callback, isAction, null);
		}

		CallbackPutActionTask(String name, URI uri, Content propertyValue, Callback callback, boolean isAction, String securityAsToken) {
			this.name = name;
			this.uri = uri;
			this.propertyValue = propertyValue;
			this.callback = callback;
			this.isAction = isAction;
			this.securityAsToken = securityAsToken;
		}

		protected void error(Exception e) {
			LOGGER.warning(e.getMessage());
			if(!isAction) {
				callback.onPutError(name, e.getMessage());
			} else {
				callback.onActionError(name);
			}
		}

		public void run() {
			try {
				URL url = uri.toURL();
				HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setRequestProperty("content-type", propertyValue.getMediaType().mediaType);
				httpCon.setRequestMethod(isAction ? "POST" : "PUT");
				if(securityAsToken != null) {
					httpCon.setRequestProperty("Authorization", "Bearer " + securityAsToken);
				}

				OutputStream out = httpCon.getOutputStream();
				out.write(propertyValue.getContent());
				out.close();

				try(InputStream is = httpCon.getInputStream()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int b;
					while ((b = is.read()) != -1) {
						baos.write(b);
					}

					String contentType = httpCon.getHeaderField("content-type");
					MediaType mediaType = MediaType.getMediaType(contentType);

					int responseCode = httpCon.getResponseCode();

					httpCon.disconnect();

					Content c = new Content(baos.toByteArray(), mediaType);

					// TODO generally all 2xx are success?
					// 204 No Content
					if (responseCode == 200 || responseCode == 204) {
						if(!isAction) {
							callback.onPut(name,  c);
						} else {
							callback.onAction(name, c);
						}
					} else {
						// error
						error(new RuntimeException("ResponseCode==" + responseCode));
					}
				}
			} catch (Exception e) {
				error(e);
			}
		}
	}

}
