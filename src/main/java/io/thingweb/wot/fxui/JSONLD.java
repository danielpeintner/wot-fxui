package io.thingweb.wot.fxui;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class JSONLD {

	public static JsonObject parseJSON(InputStream is) {
		JsonReader rdr = Json.createReader(is);

		JsonObject obj = rdr.readObject();
		// TODO sanitize JSON

		return obj;
	}

	public static String getThingName(JsonObject jobj) {
		return jobj.getString("name", null);
	}
}
