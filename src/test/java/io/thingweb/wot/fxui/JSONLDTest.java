package io.thingweb.wot.fxui;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import org.junit.Test;

import io.thingweb.wot.fxui.JSONLD.ProtocolMediaType;

public class JSONLDTest {

	@Test
	public void testCounter() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/td-counter.jsonld").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);
			
			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 1);
			
			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 3);
			
			// [{application/json} http, {application/json} ws]
			List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
			assertTrue(protocols.size() == 2);
		}
	}
	
	@Test
	public void testTestThing() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/TestThing.jsonld").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);
			
			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 6);
			
			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 7);
			
			// [{application/json} http, {application/json} coap]
			List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
			assertTrue(protocols.size() == 2);
		}
	}
	
	@Test
	public void testFestoLiveOracle() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/FestoLive-Oracle.jsonld").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);
			
			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 7);
			
			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 4);
			
			// [{application/json} https]
			List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
			assertTrue(protocols.size() == 1);
			
			// JSONLD.getInteractionHref(joInteraction, protocol)
		}
	}
	
	@Test
	public void testBlue_Pump_Fujitsu() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/Blue_Pump_Fujitsu.jsonld").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);
			
			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 6);
			
			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 3);
			
			// [{application/json} https]
			List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
			assertTrue(protocols.size() == 1);
			
			// JSONLD.getInteractionHref(joInteraction, protocol)
		}
	}
	

//	@Test
//	public void testOCF() throws IOException {
//		try(InputStream is = JSONLDTest.class.getResource("/OCF.jsonld").openStream()) {
//			JsonObject jobj = JSONLD.parseJSON(is);
//			
//			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
//			assertTrue(properties.size() == 1);
//			
//			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
//			assertTrue(actions.size() == 0);
//			
//			// [{application/json} https]
//			List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
//			assertTrue(protocols.size() == 1);
//			
//			// JSONLD.getInteractionHref(joInteraction, protocol)
//		}
//	}
	
	

}
