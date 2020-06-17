package io.thingweb.wot.fxui.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.thingweb.wot.fxui.client.Callback;
import io.thingweb.wot.fxui.client.Content;
import io.thingweb.wot.fxui.client.MediaType;
import io.thingweb.wot.fxui.client.UnsupportedException;

public class HttpClientImpl extends AbstractClientImpl {

	private final static Logger LOGGER = Logger.getLogger(HttpClientImpl.class.getName());

	private static final int NTHREDS = 5;
	private static final ExecutorService executorService = Executors.newFixedThreadPool(NTHREDS);

	public HttpClientImpl() throws Exception {
		super();
		
		// http://www.nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
		if(true){
			// Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                }
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                }
	            }
	        };
	        
	     // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	 
	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };
			
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);			
		}
	}

	public void put(String propertyName, URI uri, Content propertyValue, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException {
		try {
			CallbackPutActionTask cgt = new CallbackPutActionTask(propertyName, uri, propertyValue, callback, false, requestOptions);
			executorService.submit(cgt);
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			callback.onPutError(propertyName, e.getMessage());
		}
	}
	
	
	public void get(String propertyName, URI uri, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException {
		try {
			CallbackGetTask cgt = new CallbackGetTask(propertyName, uri, callback, requestOptions);
			executorService.submit(cgt);
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			callback.onGetError(propertyName);
		}
	}

	public void observe(String propertyName, URI uri, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException {
		callback.onObserveError(propertyName);
		// throw new UnsupportedException("Not implemented yet");
	}

	public void observeRelease(String propertyName) throws UnsupportedException {
		throw new UnsupportedException("Not implemented yet");
	}
	

	public void action(String actionName, URI uri, Content actionValue, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException {
		try {
			CallbackPutActionTask cgt = new CallbackPutActionTask(actionName, uri, actionValue, callback, true, requestOptions);
			executorService.submit(cgt);
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			callback.onActionError(actionName);
		}
	}
	
	private void addRequestOptions(HttpURLConnection httpCon, List<RequestOption> requestOptions) {		
		if(requestOptions != null) {
			for(RequestOption ro : requestOptions) {
				httpCon.setRequestProperty(ro.key, ro.value);	
			}
		}
	}

	class CallbackGetTask implements Runnable {
		private final String propertyName;
		private final URI uri;
		private final Callback callback;
		private final List<RequestOption> requestOptions;
		
		CallbackGetTask(String propertyName, URI uri, Callback callback, List<RequestOption> requestOptions) {
			this.propertyName = propertyName;
			this.uri = uri;
			this.callback = callback;
			this.requestOptions = requestOptions;
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
				addRequestOptions(httpCon, requestOptions);

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
		private final List<RequestOption> requestOptions;

		CallbackPutActionTask(String name, URI uri, Content propertyValue, Callback callback, boolean isAction, List<RequestOption> requestOptions) {
			this.name = name;
			this.uri = uri;
			this.propertyValue = propertyValue;
			this.callback = callback;
			this.isAction = isAction;
			this.requestOptions = requestOptions;
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
				addRequestOptions(httpCon, requestOptions);

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

					// generally all 2xx are success
					// 200 OK
					// 201 Created
					// 202 Accepted
					// 203 Non-Authoritative Information
					// 204 No Content
					// ...
					if (responseCode >= 200 && responseCode < 300) {
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
