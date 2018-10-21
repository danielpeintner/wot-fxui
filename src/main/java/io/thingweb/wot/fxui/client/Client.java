package io.thingweb.wot.fxui.client;

import java.net.URI;
import java.util.List;

public interface Client {
	
	// RequestOption could be something like 
	// * "Authorization", "Bearer " + securityAsToken
	// * "Authorization", "Basic " + "user:pwd"
	public static class RequestOption {
		
		public final String key;
		public final int number;
		
		public final String value;
		
		public RequestOption(String key, int number, String value) {
			this.key = key;
			this.number = number;
			this.value = value;
		}
	}
	
	public void put(String propertyName, URI uri, Content propertyValue, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException;
	
	public void get(String propertyName, URI uri, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException;

	public void observe(String propertyName, URI uri, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException;

	public void observeRelease(String propertyName) throws UnsupportedException;
	
	public void action(String actionName, URI uri, Content actionValue, Callback callback, List<RequestOption> requestOptions) throws UnsupportedException;

}
