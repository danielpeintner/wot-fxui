package io.thingweb.wot.fxui;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.json.JsonObject;

import org.junit.Test;

import io.thingweb.wot.fxui.JSONLD.Form;

public class JSONLDTest {

	@Test
	public void testFoo() throws IOException {
		InputStream is = JSONLDTest.class.getResource("/td-sample1.jsonld").openStream();
		JsonObject jobj= JSONLD.parseJSON(is);

		List<String> props = JSONLD.getProperties(jobj);
		assertTrue(props.contains("status"));
		assertTrue(props.contains("brightness"));
		assertTrue(props.size() == 2);
		assertTrue(JSONLD.isPropertyWritable(jobj, "status") == true);
		assertTrue(JSONLD.isPropertyWritable(jobj, "brightness") == false);
		assertTrue(JSONLD.isPropertyObservable(jobj, "status") == true);
		assertTrue(JSONLD.isPropertyObservable(jobj, "brightness") == false);
		{
			List<Form> formsStatus = JSONLD.getPropertyForms(jobj, "status");
			assertTrue(formsStatus.size() == 1);
			assertTrue(formsStatus.get(0).href.equals("coaps://mylamp.example.com:5683/status"));
			assertTrue(formsStatus.get(0).mediaType.equals("application/json"));

			List<Form> formsBrightness = JSONLD.getPropertyForms(jobj, "brightness");
			assertTrue(formsBrightness.size() == 1);
			assertTrue(formsBrightness.get(0).href.equals("coaps://mylamp.example.com:5683/brightness"));
			assertTrue(formsBrightness.get(0).mediaType.equals("application/exi"));
		}

		List<String> acs = JSONLD.getActions(jobj);
		assertTrue(acs.contains("toggle"));
		assertTrue(acs.size() == 1);
		{
			List<Form> formsToggle = JSONLD.getPropertyForms(jobj, "toggle");
			assertTrue(formsToggle.size() == 1);
			assertTrue(formsToggle.get(0).href.equals("coaps://mylamp.example.com:5683/toggle"));
			assertTrue(formsToggle.get(0).mediaType.equals("application/json"));
		}

		List<String> evs = JSONLD.getEvents(jobj);
		assertTrue(evs.contains("overheating"));
		assertTrue(evs.size() == 1);
		{
			List<Form> formsOverheating = JSONLD.getPropertyForms(jobj, "overheating");
			assertTrue(formsOverheating.size() == 1);
			assertTrue(formsOverheating.get(0).href.equals("coaps://mylamp.example.com:5683/oh"));
			assertTrue(formsOverheating.get(0).mediaType.equals("application/json"));
		}

	}
}
