package io.thingweb.wot.fxui;

import java.io.IOException;
import java.io.InputStream;

import javax.json.JsonObject;

import org.junit.Test;

public class JSONLDTest {

	@Test
	public void testCounter() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/td-counter.jsonld").openStream()) {
			@SuppressWarnings("unused")
			JsonObject jobj = JSONLD.parseJSON(is);
		}
	}

}
