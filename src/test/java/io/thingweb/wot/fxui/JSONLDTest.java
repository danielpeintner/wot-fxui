package io.thingweb.wot.fxui;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.json.JsonObject;

import org.junit.Test;

public class JSONLDTest {

	@Test
	public void testCounter() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/td-counter.jsonld").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);
			
			List<JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 1);
			
			List<JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 3);
		}
	}
	
	@Test
	public void testTestThing() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/TestThing.jsonld").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);
			
			List<JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 6);
			
			List<JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 7);
			
		}
	}
	
	@Test
	public void testFestoLiveOracle() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/FestoLive-Oracle.jsonld").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);
			
			List<JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 7);
			
			List<JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 4);
			
		}
	}

}
