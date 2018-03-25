package io.thingweb.wot.fxui;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class MainLayoutController {

	private final static Logger LOGGER = Logger.getLogger(MainLayoutController.class.getName());

	@FXML
	BorderPane borderPaneRoot;

	@FXML
	TabPane tabPane;

	@FXML
	protected void handleMenuOpenTD(ActionEvent event) {
		try {
			Window owner = borderPaneRoot.getScene().getWindow();
			FileChooser fc = new FileChooser();
			File f = fc.showOpenDialog(owner);
			if (f != null) {
				LOGGER.info("Load file: " + f);
				JsonObject jobj = JSONLD.parseJSON(new FileInputStream(f));
				Tab t = new Tab(JSONLD.getThingName(jobj));
				tabPane.getTabs().add(t);
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			showAlertDialog(e);
		}
	}

	public static void showAlertDialog(Exception e) {
		showAlertDialog(e, "");
	}

	// http://code.makery.ch/blog/javafx-dialogs-official/
	public static void showAlertDialog(Exception e, String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception Dialog");
		alert.setHeaderText("Ein Fehler ist aufgetreten " + (msg == null ? "" : msg));
		alert.setContentText(e.getMessage()); // "Could not find file
												// blabla.txt!");

		// Exception ex = new FileNotFoundException("Could not find file
		// blabla.txt");

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}

	@FXML
	protected void handleMenuClose(ActionEvent event) {
		System.exit(0);
	}
}
