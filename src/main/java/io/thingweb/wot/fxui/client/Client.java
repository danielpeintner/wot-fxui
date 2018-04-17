package io.thingweb.wot.fxui.client;

import java.net.URI;

public interface Client {

	public void put(String propertyName, URI uri, Content propertyValue, Callback callback) throws UnsupportedException;

	public void put(String propertyName, URI uri, Content propertyValue, Callback callback, String securityAsToken) throws UnsupportedException;

	public void get(String propertyName, URI uri, Callback callback) throws UnsupportedException;

	public void get(String propertyName, URI uri, Callback callback, String securityAsToken) throws UnsupportedException;

	public void observe(String propertyName, URI uri, Callback callback) throws UnsupportedException;

	public void observe(String propertyName, URI uri, Callback callback, String securityAsToken) throws UnsupportedException;

	public void observeRelease(String propertyName) throws UnsupportedException;

	public void action(String actionName, URI uri, Content actionValue, Callback callback) throws UnsupportedException;

	public void action(String actionName, URI uri, Content actionValue, Callback callback, String securityAsToken) throws UnsupportedException;

}
