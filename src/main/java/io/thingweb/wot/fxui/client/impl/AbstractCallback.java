package io.thingweb.wot.fxui.client.impl;

import io.thingweb.wot.fxui.client.Callback;
import io.thingweb.wot.fxui.client.Content;

public class AbstractCallback implements Callback {

    public static final String MESSAGE = "unexpected %s callback: %s on %s";

    private static void throwEx(String method, String target,boolean error) {
        throw new RuntimeException(String.format(MESSAGE,error? "error" : "success",method,target));
    }

    @Override
    public void onGet(String propertyName, Content response) {
        throwEx("GET",propertyName,false);
    }

    @Override
    public void onGetError(String propertyName) {
        throwEx("GET",propertyName,true);
    }

    @Override
    public void onPut(String propertyName, Content response) {
        throwEx("PUT",propertyName,false);
    }

    @Override
    public void onPutError(String propertyName, String message) {
        throwEx("PUT",propertyName,true);
    }

    @Override
    public void onObserve(String propertyName, Content response) {
        throwEx("GET+OBSERVE",propertyName,false);
    }

    @Override
    public void onObserveError(String propertyName) {
        throwEx("GET+OBSERVE",propertyName,true);
    }

    @Override
    public void onAction(String actionName, Content response) {
        throwEx("POST",actionName,false);
    }

    @Override
    public void onActionError(String actionName) {
        throwEx("POST",actionName,true);
    }
}