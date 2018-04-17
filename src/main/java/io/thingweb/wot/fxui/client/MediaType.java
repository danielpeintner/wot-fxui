package io.thingweb.wot.fxui.client;

import java.util.HashMap;
import java.util.Map;

public enum MediaType {
	// TODO decide whether to create new independent ones or re-use org.eclipse.californium.core.coap.MediaTypeRegistry
	// Note: http server uses again other ones. Hence it might be good to be independent

	/** text/plain */
	TEXT_PLAIN("text/plain"),
	/** text/html */
	TEXT_HTML("text/html"),
	/** application/xml */
	APPLICATION_XML("application/xml"),
	/** application/exi */
	APPLICATION_EXI("application/exi"),
	/** application/json */
	APPLICATION_JSON("application/json"),
	/** undefined/unknown */
	UNDEFINED("undefined");

	public final String mediaType;

	static Map<String, MediaType> mediaTypes = new HashMap<>();
	static {
		mediaTypes.put(TEXT_PLAIN.mediaType, TEXT_PLAIN);
		mediaTypes.put(TEXT_HTML.mediaType, TEXT_HTML);
		mediaTypes.put(APPLICATION_XML.mediaType, APPLICATION_XML);
		mediaTypes.put(APPLICATION_EXI.mediaType, APPLICATION_EXI);
		mediaTypes.put(APPLICATION_JSON.mediaType, APPLICATION_JSON);
		// mediaTypes.put(UNDEFINED.mediaType, UNDEFINED);
	}

	public static MediaType getMediaType(String mediaType) {
		return mediaTypes.get(mediaType);
	}


	MediaType(String mediaType) {
		this.mediaType = mediaType;
	}
}
