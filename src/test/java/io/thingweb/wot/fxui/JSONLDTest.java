package io.thingweb.wot.fxui;

import java.io.IOException;
import java.io.InputStream;

import javax.json.JsonObject;

import org.junit.Test;

public class JSONLDTest {

	@Test
	public void testCounter() throws IOException {
		InputStream is = JSONLDTest.class.getResource("/td-counter.jsonld").openStream();
		JsonObject jobj = JSONLD.parseJSON(is);
	}

}
