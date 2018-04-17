package io.thingweb.wot.fxui.client;

import java.net.URI;

import io.thingweb.wot.fxui.client.impl.CoapClientImpl;
import io.thingweb.wot.fxui.client.impl.HttpClientImpl;

public class ClientFactory {

	boolean isCoapScheme(String scheme) {
		return("coap".equals(scheme) || "coaps".equals(scheme));
	}

	boolean isHttpScheme(String scheme) {
		return("http".equals(scheme) || "https".equals(scheme));
	}

	public Client getClient(URI uri) throws UnsupportedException {
		String scheme = uri.getScheme();
		// check uri scheme
		if(isHttpScheme(scheme)) {
			Client client = new HttpClientImpl();
			return client;
		} else if(isCoapScheme(scheme)) {
			Client client = new CoapClientImpl();
			return client;
		} else {
			throw new UnsupportedException("No client found for scheme: " + scheme);
			// return getClientUrl(jsonld.toURL());
		}
	}

}
