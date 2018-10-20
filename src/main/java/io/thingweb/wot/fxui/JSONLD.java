package io.thingweb.wot.fxui;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class JSONLD {
	
	private final static Logger LOGGER = Logger.getLogger(JSONLD.class.getName());

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
			return parseJSON(rdr);
		}
	}
	
	public static JsonObject parseJSON(String text) {
		try(JsonReader rdr = Json.createReader(new StringReader(text))) {
			return parseJSON(rdr);
		}
	}
	
	private static JsonObject parseJSON(JsonReader rdr) {
		JsonObject obj = rdr.readObject();
		// TODO sanitize JSON ?
		return obj;
	}

	public static String getThingName(JsonObject jobj) {
		return jobj.getString("name", null);
	}
	
	public static String getBase(JsonObject jobj) {
		return jobj.getString("base", null);
	}


	public static Map<String, JsonObject> getProperties(JsonObject jobj) {
		return getInteractions(jobj, "properties");
	}

	public static Map<String, JsonObject> getActions(JsonObject jobj) {
		return getInteractions(jobj, "actions");
	}

	private static Map<String, JsonObject> getInteractions(JsonObject jobj, String key) {
		Map<String, JsonObject> list = new HashMap<>();

		JsonValue interactions = jobj.get(key);
		if (interactions != null && interactions.getValueType() == ValueType.OBJECT) {
			JsonObject joInteractions = interactions.asJsonObject();
			Set<String> keys = joInteractions.keySet();
			if (keys.size() > 0) {
				for (final String interactionName : keys) {
					JsonValue jvInteraction = joInteractions.get(interactionName);
					if (jvInteraction != null && jvInteraction.getValueType() == ValueType.OBJECT) {
						JsonObject joInteraction = jvInteraction.asJsonObject();
						list.put(interactionName, joInteraction);
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

	
	private static String getAbsoluteUri(String base, String href) {
		if(base == null) {
			return href;
		} else {
			// add base to interactions if relative URI
			try {
				URI u = new URI(href);
				if(u.isAbsolute()) {
					// ok as is
					return href;
				} else {
					// add base
					return base + href;
				}
				
			} catch (URISyntaxException e) {
			}
			return null;
		}
	}

	public static List<ProtocolMediaType> getProtocols(JsonObject jobj) {
		List<ProtocolMediaType> pms = new ArrayList<>();

		Map<String, JsonObject> properties = getProperties(jobj);
		Map<String, JsonObject> actions = getActions(jobj);

		List<JsonObject> interactions = new ArrayList<>();
		for(JsonObject jo : properties.values()) {
			interactions.add(jo);
		}
		for(JsonObject jo : actions.values()) {
			interactions.add(jo);
		}
		// interactions.addAll(properties);
		// interactions.addAll(actions);
		
		String base = getBase(jobj);

		for(JsonObject joInteraction :  interactions) {
			if (joInteraction.containsKey("forms") && joInteraction.get("forms").getValueType() == ValueType.ARRAY) {
				JsonArray jaForms = joInteraction.get("forms").asJsonArray();
				for(int i=0; i<jaForms.size(); i++) {
					if (jaForms.get(i) != null && jaForms.get(i).getValueType() == ValueType.OBJECT) {
						JsonObject joForm = jaForms.get(i).asJsonObject();

						if (joForm.containsKey("href") && joForm.get("href").getValueType() == ValueType.STRING) {
							String href = joForm.getString("href");
							href = getAbsoluteUri(base, href);
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
	
	
	public static String getInteractionHref(JsonObject joInteraction, String base, String protocol) {
		if (joInteraction.containsKey(JSONLD.KEY_FORMS) && joInteraction.get(JSONLD.KEY_FORMS).getValueType() == ValueType.ARRAY) {
			JsonArray jaForms = joInteraction.get(JSONLD.KEY_FORMS).asJsonArray();
			for(int i=0; i<jaForms.size(); i++) {
				// pick right form / mediaType
				if (jaForms.get(i) != null && jaForms.get(i).getValueType() == ValueType.OBJECT) {
					JsonObject joForm = jaForms.get(i).asJsonObject();

					if (joForm.containsKey(JSONLD.KEY_HREF) && joForm.get(JSONLD.KEY_HREF).getValueType() == ValueType.STRING) {
						String href = joForm.getString(JSONLD.KEY_HREF);
						href = getAbsoluteUri(base, href);
						if(href.startsWith(protocol)) {
							return href;
						}
					}
				}

			}
		} else {
			LOGGER.warning("Property forms not array or null");
		}

		return null; // failure
	}
}
