package io.thingweb.wot.fxui;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class JSONLD {

	public static String KEY_PROPERTIES = "properties";
	public static String KEY_ACTIONS = "actions";
	public static String KEY_EVENTS = "events";

	public static String KEY_FORMS = "forms";
	public static String KEY_HREF = "href";

	public static String KEY_WRITABLE = "writable";
	public static String KEY_OBSERVABLE = "observable";

	public static class Form {
		public final String href;
		public final String mediaType;

		public Form(final String href, final String mediaType) {
			this.href = href;
			this.mediaType = mediaType;
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof Form) {
				Form other = (Form) o;
				boolean eqHref = (this.href == other.href || (this.href != null && this.href.equals(other.href)));
				boolean eqMediaType = (this.mediaType == other.mediaType || (this.mediaType != null && this.mediaType.equals(other.mediaType)));
				return (eqHref && eqMediaType);
			}
			return false;
		}

		@Override
		public String toString() {
			return "{" + mediaType + "} " + href;
		}
	}

	public static class ProtocolMediaType {
		public final String protocol;
		public final String mediaType;

		public ProtocolMediaType(final String protocol, final String mediaType) {
			this.protocol = protocol;
			this.mediaType = mediaType;
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof ProtocolMediaType) {
				ProtocolMediaType other = (ProtocolMediaType) o;
				boolean eqProtocol = (this.protocol == other.protocol || (this.protocol != null && this.protocol.equals(other.protocol)));
				boolean eqMediaType = (this.mediaType == other.mediaType || (this.mediaType != null && this.mediaType.equals(other.mediaType)));
				return (eqProtocol && eqMediaType);
			}
			return false;
		}

		@Override
		public String toString() {
			return "{" + mediaType + "} " + protocol;
		}
	}


	public static JsonObject parseJSON(InputStream is) {
		try(JsonReader rdr = Json.createReader(is)) {
			JsonObject obj = rdr.readObject();
			// TODO sanitize JSON

			return obj;
		}
	}
	
	public static JsonObject parseJSON(String text) {
		try(JsonReader rdr = Json.createReader(new StringReader(text))) {
			JsonObject obj = rdr.readObject();
			// TODO sanitize JSON

			return obj;
		}
	}

	public static String getThingName(JsonObject jobj) {
		return jobj.getString("name", null);
	}


	public static List<JsonObject> getProperties(JsonObject jobj) {
		return getInteractions(jobj, "properties");
	}

	public static List<JsonObject> getActions(JsonObject jobj) {
		return getInteractions(jobj, "actions");
	}

	private static List<JsonObject> getInteractions(JsonObject jobj, String key) {
		List<JsonObject> list = new ArrayList<>();

		JsonValue interactions = jobj.get(key);
		if (interactions != null && interactions.getValueType() == ValueType.OBJECT) {
			JsonObject joInteractions = interactions.asJsonObject();
			Set<String> keys = joInteractions.keySet();
			if (keys.size() > 0) {
				for (final String interactionName : keys) {
					JsonValue jvInteraction = joInteractions.get(interactionName);
					if (jvInteraction != null && jvInteraction.getValueType() == ValueType.OBJECT) {
						JsonObject joInteraction = jvInteraction.asJsonObject();
						list.add(joInteraction);
					}
				}
			}
		}

		return list;
	}

	public static String getProtocol (String href) {
		// Not URL does not work for unknown protocols such as ws://
		// URL url = new URL(href);
		if(href != null) {
			href = href.trim();
			int index = href.indexOf("://");
			if(index > 0) {
				return href.substring(0, index);
			}
		}
		return null;
	}


	public static List<ProtocolMediaType> getProtocols(JsonObject jobj) {
		List<ProtocolMediaType> pms = new ArrayList<>();

		List<JsonObject> properties = getProperties(jobj);
		List<JsonObject> actions = getActions(jobj);

		List<JsonObject> interactions = new ArrayList<>();
		interactions.addAll(properties);
		interactions.addAll(actions);

		for(JsonObject joInteraction :  interactions) {
			if (joInteraction.containsKey("forms") && joInteraction.get("forms").getValueType() == ValueType.ARRAY) {
				JsonArray jaForms = joInteraction.get("forms").asJsonArray();
				for(int i=0; i<jaForms.size(); i++) {
					if (jaForms.get(i) != null && jaForms.get(i).getValueType() == ValueType.OBJECT) {
						JsonObject joForm = jaForms.get(i).asJsonObject();

						if (joForm.containsKey("href") && joForm.get("href").getValueType() == ValueType.STRING) {
							String href = joForm.getString("href");
							String protocol = getProtocol(href);
							if(protocol != null) {
								if (joForm.containsKey("mediaType") && joForm.get("mediaType").getValueType() == ValueType.STRING) {
									String mediaType = joForm.getString("mediaType");

									ProtocolMediaType pm = new ProtocolMediaType(protocol, mediaType);
									if(!pms.contains(pm)) {
										pms.add(pm);
									}
								}
							}
						}
					}

				}
			}
		}

		return pms;
	}
}
