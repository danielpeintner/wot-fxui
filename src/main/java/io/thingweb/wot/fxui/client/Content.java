package io.thingweb.wot.fxui.client;

import java.util.Arrays;

public class Content {

	final byte[] content;
	final MediaType mediaType;

	public Content(byte[] content, MediaType mediaType) {
		this.content = content;
		this.mediaType = mediaType;
	}

	public MediaType getMediaType() {
		return this.mediaType;
	}

	public byte[] getContent() {
		return this.content;
	}

	@Override
	public boolean equals(Object o) {
		if(o != null && o instanceof Content) {
			Content other = (Content) o;
			if(this.mediaType == other.mediaType) {
				return Arrays.equals(this.content, other.content);
			}
		}
		return false;
	}
}
