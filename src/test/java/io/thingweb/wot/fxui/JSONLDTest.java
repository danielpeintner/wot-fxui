package io.thingweb.wot.fxui;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import org.junit.Test;

import io.thingweb.wot.fxui.JSONLD.Form;
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

	@Test
	public void testLight_local() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/td-light-local.json").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);

			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 2);

			String base = JSONLD.getBase(jobj);

			// [{application/json} http]
			List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
			assertTrue(protocols.size() == 1);

			for(ProtocolMediaType protocol : protocols) {
				for(String propName : properties.keySet()) {
					JsonObject joProperty = properties.get(propName);

					Form form = JSONLD.getInteractionForm(joProperty, base, protocol.protocol);

					// TODO check form with POST for setting data (instead of PUT)

					if(propName.equals("SwitchState")) {
						assertTrue(form.href.equals(base + "/light"));
					} else if(propName.equals("CurrentBrightness")) {
						assertTrue(form.href.equals(base + "/light"));
					}
				}
			}


			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 3);



			// JSONLD.getInteractionHref(joInteraction, protocol)
		}
	}

	@Test
	public void testGrid() throws IOException {
		try(InputStream is = JSONLDTest.class.getResource("/grid.json").openStream()) {
			JsonObject jobj = JSONLD.parseJSON(is);

			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
			assertTrue(properties.size() == 11);

			String base = JSONLD.getBase(jobj);
			assertTrue("http://XXX_TBD_XXX.compute.amazonaws.com:8989/cem/".equals(base));

			// [{application/json} http]
			List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
			assertTrue(protocols.size() == 1);

			for(ProtocolMediaType protocol : protocols) {
				for(String propName : properties.keySet()) {
					JsonObject joProperty = properties.get(propName);

					Form form = JSONLD.getInteractionForm(joProperty, base, protocol.protocol);

					// TODO check form with POST for setting data (instead of PUT)

					if(propName.equals("SwitchState")) {
						assertTrue(form.href.equals(base + "/light"));
					} else if(propName.equals("CurrentBrightness")) {
						assertTrue(form.href.equals(base + "/light"));
					}
				}
			}


			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
			assertTrue(actions.size() == 0);


//			Map<String, JsonObject> events = JSONLD.getEvents(jobj);
//			assertTrue(events.size() == 1);



			// JSONLD.getInteractionHref(joInteraction, protocol)
		}
	}


}
