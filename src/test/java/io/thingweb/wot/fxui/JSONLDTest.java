package io.thingweb.wot.fxui;

import java.io.File;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class JSONLDTest {

	@Test
	public void testFoo() throws IOException {
		InputStream is = JSONLDTest.class.getResource("/td-sample1.jsonld").openStream();
		JsonObject jobj= JSONLD.parseJSON(is);

		List<String> props = JSONLD.getProperties(jobj);
		assertTrue(props.contains("status"));
		assertTrue(props.size() == 1);

		List<String> acs = JSONLD.getActions(jobj);
		assertTrue(acs.contains("toggle"));
		assertTrue(acs.size() == 1);

		List<String> evs = JSONLD.getEvents(jobj);
		assertTrue(evs.contains("overheating"));
		assertTrue(evs.size() == 1);

	}
}
