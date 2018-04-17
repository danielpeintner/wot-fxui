package io.thingweb.wot.fxui.client;

public interface Callback {

	public void onGet(String propertyName, Content response);

	public void onGetError(String propertyName);

	public void onPut(String propertyName, Content response);

	public void onPutError(String propertyName, String message);

	public void onObserve(String propertyName, Content response);

	public void onObserveError(String propertyName);

	public void onAction(String actionName, Content response);

	public void onActionError(String actionName);
}
