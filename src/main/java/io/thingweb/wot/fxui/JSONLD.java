package io.thingweb.wot.fxui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class JSONLD {

	public static class Form {
		public final String href;
		public final String mediaType;

		public Form(final String href, final String mediaType) {
			this.href = href;
			this.mediaType = mediaType;
		}
	}

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


	public static boolean isPropertyWritable(JsonObject jobj, String propertyName) {
		JsonArray inters = getJSONInteractions(jobj);

		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
			// "@type": ["Property"]
			JsonArray type = interaction.getJsonArray("@type");
			for(JsonString stype : type.getValuesAs(JsonString.class)) {
				if(stype.getChars().equals("Property")) {
					// System.out.println("Property for " + interaction.getString("name"));
					if(propertyName.equals(interaction.getString("name"))) {
						return interaction.getBoolean("writable", false);
					}
				}
			}
		}

		return false;
	}

	public static boolean isPropertyObservable(JsonObject jobj, String propertyName) {
		JsonArray inters = getJSONInteractions(jobj);

		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
			// "@type": ["Property"]
			JsonArray type = interaction.getJsonArray("@type");
			for(JsonString stype : type.getValuesAs(JsonString.class)) {
				if(stype.getChars().equals("Property")) {
					// System.out.println("Property for " + interaction.getString("name"));
					if(propertyName.equals(interaction.getString("name"))) {
						return interaction.getBoolean("observable", false);
					}
				}
			}
		}

		return false;
	}

	public static List<Form> getPropertyForms(JsonObject jobj, String propertyName) {
		JsonArray inters = getJSONInteractions(jobj);

		List<Form> propForms = getInteractionForms(inters, propertyName);

//		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
//			// "@type": ["Property"]
//			JsonArray type = interaction.getJsonArray("@type");
//			for(JsonString stype : type.getValuesAs(JsonString.class)) {
//				if(stype.getChars().equals("Property")) {
//					// System.out.println("Property for " + interaction.getString("name"));
//					if(propertyName.equals(interaction.getString("name"))) {
//						JsonValue jvForm = interaction.get("form");
//						if(jvForm.getValueType() == ValueType.ARRAY) {
//							JsonArray jaForm = jvForm.asJsonArray();
//							for(JsonValue jv : jaForm) {
//								if(jv.getValueType() == ValueType.OBJECT) {
//									JsonObject jo = jv.asJsonObject();
//									String href = jo.getString("href", null);
//									String mediaType = jo.getString("mediaType", "application/json");
//									Form f = new Form(href, mediaType);
//									propForms.add(f);
//								}
//							}
//						}
//					}
//				}
//			}
//		}

		return propForms;
	}

	public static List<Form> getEventForms(JsonObject jobj, String eventName) {
		JsonArray inters = getJSONInteractions(jobj);
		List<Form> propForms = getInteractionForms(inters, eventName);
		return propForms;
	}

	public static List<Form> getActionForms(JsonObject jobj, String actionName) {
		JsonArray inters = getJSONInteractions(jobj);
		List<Form> propForms = getInteractionForms(inters, actionName);
		return propForms;
	}

	private static List<Form> getInteractionForms(JsonArray inters, String name) {
		List<Form> interForms = new ArrayList<>();

		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
//			JsonArray type = interaction.getJsonArray("@type");
//			for(JsonString stype : type.getValuesAs(JsonString.class)) {
//				if(stype.getChars().equals("Property")) {
					if(name.equals(interaction.getString("name"))) {
						JsonValue jvForm = interaction.get("form");
						if(jvForm.getValueType() == ValueType.ARRAY) {
							JsonArray jaForm = jvForm.asJsonArray();
							for(JsonValue jv : jaForm) {
								if(jv.getValueType() == ValueType.OBJECT) {
									JsonObject jo = jv.asJsonObject();
									String href = jo.getString("href", null);
									String mediaType = jo.getString("mediaType", "application/json");
									Form f = new Form(href, mediaType);
									interForms.add(f);
								}
							}
						}
					}
//				}
//			}
		}

		return interForms;
	}

	public static List<String> getProperties(JsonObject jobj) {
		List<String> props = new ArrayList<>();

		JsonArray inters = getJSONInteractions(jobj);

		for (JsonObject interaction : inters.getValuesAs(JsonObject.class)) {
			// "@type": ["Property"]
			JsonArray type = interaction.getJsonArray("@type");
			for(JsonString stype : type.getValuesAs(JsonString.class)) {
				if(stype.getChars().equals("Property")) {
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
