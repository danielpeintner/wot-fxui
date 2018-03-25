package io.thingweb.wot.fxui;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

public class JSONLD {

//	enum InteractionType {
//		Property,
//		Action,
//		Event
//	}


	public static JsonObject parseJSON(InputStream is) {
		JsonReader rdr = Json.createReader(is);

		JsonObject obj = rdr.readObject();
		return obj;
	}

	static JsonArray getJSONInteractions(JsonObject jobj) {
		JsonArray inters = jobj.getJsonArray("interaction");
		return inters;
	}


	public static List<String> getProperties(JsonObject jobj) {
		List<String> props = new ArrayList<>();

		JsonArray inters = getJSONInteractions(jobj);

		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
			// "@type": ["Property"]
			JsonArray type = interaction.getJsonArray("@type");
			for(JsonString stype : type.getValuesAs(JsonString.class)) {
				if(stype.getChars().equals("Property")) {
					System.out.println("Property for " + interaction.getString("name"));
					props.add(interaction.getString("name"));
				}
//				else if(stype.getChars().equals("Action")) {
//					System.out.println("Action for " + interaction.getString("name"));
//				} else if(stype.getChars().equals("Event")) {
//					System.out.println("Event for " + interaction.getString("name"));
//				} else {
//					System.err.println("TODO" + type);
//				}
			}
		}

		return props;
	}

	public static List<String> getActions(JsonObject jobj) {
		List<String> acs = new ArrayList<>();

		JsonArray inters = getJSONInteractions(jobj);

		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
			JsonArray type = interaction.getJsonArray("@type");
			for(JsonString stype : type.getValuesAs(JsonString.class)) {
				if(stype.getChars().equals("Action")) {
					acs.add(interaction.getString("name"));
				}
			}
		}

		return acs;
	}

	public static String getThingName(JsonObject jobj) {
		return jobj.getString("name", null);
	}

	public static List<String> getEvents(JsonObject jobj) {
		List<String> evs = new ArrayList<>();

		JsonArray inters = getJSONInteractions(jobj);

		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
			JsonArray type = interaction.getJsonArray("@type");
			for(JsonString stype : type.getValuesAs(JsonString.class)) {
				if(stype.getChars().equals("Event")) {
					evs.add(interaction.getString("name"));
				}
			}
		}

		return evs;
	}
}
