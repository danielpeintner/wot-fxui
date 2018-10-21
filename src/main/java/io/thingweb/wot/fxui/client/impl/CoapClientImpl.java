package io.thingweb.wot.fxui.client.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.MessageObserver;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;

import io.thingweb.wot.fxui.client.Callback;
import io.thingweb.wot.fxui.client.Content;
import io.thingweb.wot.fxui.client.MediaType;

public class CoapClientImpl extends AbstractClientImpl {

	private final static Logger LOGGER = Logger.getLogger(HttpClientImpl.class.getName());

	final int SECURITY_TOKEN_NUMBER = 65000;
	final String SECURITY_BEARER_STRING = "Bearer ";

	Map<String, ObserveRelation> observes = new HashMap<>();

	public CoapClientImpl() {
		super();
	}
	
	private void addRequestOptions(Request request, List<RequestOption> requestOptions) {
		if(requestOptions != null) {
			for(RequestOption ro : requestOptions) {
				// Option tokenOption = new Option(SECURITY_TOKEN_NUMBER, (SECURITY_BEARER_STRING + securityAsToken));
				Option tokenOption = new Option(ro.number, ro.value);
				request.getOptions().addOption(tokenOption);	
			}
		}		
	}
	
	public void put(final String propertyName, URI uri, final Content propertyValue, final Callback callback, final List<RequestOption> requestOptions) {
		CoapClient coap = new CoapClient(uri);

		LOGGER.info("CoAP PUT " + coap.getURI() + " (RequestOptions=" + requestOptions + ") with: " + new String(propertyValue.getContent()));

		Request request = Request.newPut();
		request.setPayload(propertyValue.getContent());
		request.getOptions().setContentFormat(getCoapContentFormat(propertyValue.getMediaType()));
		
		addRequestOptions(request, requestOptions);
		
		// asynchronous
		coap.advanced(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				if(ResponseCode.isSuccess(response.getCode()) ) {
					Content content = new Content(response.getPayload(), getMediaType(response.getOptions()));
					callback.onPut(propertyName, content);
				} else {
					error(response.getCode().name());
				}
			}

			void error(String message) {
				callback.onPutError(propertyName, message);
			}

			@Override
			public void onError() {
				error("Network error");
			}
		}, request);
	}

	public void get(final String propertyName, URI uri, final Callback callback, final List<RequestOption> requestOptions) {
		CoapClient coap = new CoapClient(uri);

		LOGGER.info("CoAP get " + coap.getURI() + " (RequestOptions=" + requestOptions + ")");

		Request request = Request.newGet();
		addRequestOptions(request, requestOptions);

		// asynchronous
		coap.advanced(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				if(ResponseCode.isSuccess(response.getCode()) ) {
					Content content = new Content(response.getPayload(), getMediaType(response.getOptions()));
					callback.onGet(propertyName, content);
				} else {
					error();
				}
			}

			void error() {
				callback.onGetError(propertyName);
			}

			@Override
			public void onError() {
				error();
			}
		}, request);
	}
	

	public void observe(final String propertyName, URI uri, final Callback callback, final List<RequestOption> requestOptions) {
		CoapClient coap = new CoapClient(uri);

		LOGGER.info("CoAP observe " + coap.getURI() + " (RequestOptions=" + requestOptions + ")");

		Request request = Request.newGet().setObserve();
		addRequestOptions(request, requestOptions);

		// asynchronous
		coap.advanced(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				if(ResponseCode.isSuccess(response.getCode()) ) {
					Content content = new Content(response.getPayload(), getMediaType(response.getOptions()));
					callback.onObserve(propertyName, content);
				} else {
					error();
				}
			}

			void error() {
				callback.onObserveError(propertyName);
			}

			@Override
			public void onError() {
				error();
			}
		}, request);

		observes.put(propertyName, new ObserveRelation(request, coap));
	}

	class ObserveRelation {
		final Request request;
		final CoapClient coap;
		public ObserveRelation(Request request, CoapClient coap) {
			this.request = request;
			this.coap = coap;
		}
	}

	protected void proactiveCancel(ObserveRelation or) {
		Request request = or.request;

		Request cancel = Request.newGet();
		// copy options, but set Observe to cancel
		cancel.setOptions(request.getOptions());
		cancel.setObserveCancel();
		// use same Token
		cancel.setToken(request.getToken());
//		cancel.setDestination(request.getDestination());
//		cancel.setDestinationPort(request.getDestinationPort());
		// dispatch final response to the same message observers
		for (MessageObserver mo: request.getMessageObservers())
			cancel.addMessageObserver(mo);
		// endpoint.sendRequest(cancel);
		or.coap.advanced(cancel);
		// cancel old ongoing request
		request.cancel();
		// setCanceled(true);
	}


	public void observeRelease(String propertyName) {
		proactiveCancel(observes.remove(propertyName));
	}

	public void action(final String actionName, URI uri, Content actionValue, final Callback callback, final List<RequestOption> requestOptions) {
		CoapClient coap = new CoapClient(uri);

		LOGGER.info("CoAP post " + coap.getURI() + " (RequestOptions=" + requestOptions + ")");

		Request request = Request.newPost();
		request.setPayload(actionValue.getContent());
		request.getOptions().setContentFormat(getCoapContentFormat(actionValue.getMediaType()));

		addRequestOptions(request, requestOptions);

		// asynchronous
		coap.advanced(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				Content content = new Content(response.getPayload(), getMediaType(response.getOptions()));
				callback.onAction(actionName, content);
			}

			@Override
			public void onError() {
				callback.onActionError(actionName);
			}
		}, request);
	}


	public static MediaType getMediaType(OptionSet os) {
		MediaType mt;
		if (os.getContentFormat() == -1) {
			// undefined
			mt = MediaType.APPLICATION_JSON;
		} else {
			String mediaType = MediaTypeRegistry.toString(os.getContentFormat());
			mt = MediaType.getMediaType(mediaType);
		}
		return mt;
	}

	public static int getCoapContentFormat(MediaType mediaType) {
		int contentFormat;
		switch (mediaType) {
			case TEXT_PLAIN:
				contentFormat = MediaTypeRegistry.TEXT_PLAIN;
				break;
			case APPLICATION_XML:
				contentFormat = MediaTypeRegistry.APPLICATION_XML;
				break;
			case APPLICATION_EXI:
				contentFormat = MediaTypeRegistry.APPLICATION_EXI;
				break;
			case APPLICATION_JSON:
				contentFormat = MediaTypeRegistry.APPLICATION_JSON;
				break;
			default:
				// TODO how to deal best?
				contentFormat = MediaTypeRegistry.UNDEFINED;
		}
		return contentFormat;
	}

}
