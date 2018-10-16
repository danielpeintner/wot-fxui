package io.thingweb.wot.fxui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;

import io.thingweb.wot.fxui.JSONLD.ProtocolMediaType;
import io.thingweb.wot.fxui.client.Callback;
import io.thingweb.wot.fxui.client.Client;
import io.thingweb.wot.fxui.client.ClientFactory;
import io.thingweb.wot.fxui.client.Content;
import io.thingweb.wot.fxui.client.MediaType;
import io.thingweb.wot.fxui.client.impl.AbstractCallback;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class MainLayoutController {

	private final static Logger LOGGER = Logger.getLogger(MainLayoutController.class.getName());

	@FXML
	StackPane stackPaneRoot;

	@FXML
	BorderPane borderPaneRoot;

	@FXML
	TabPane tabPane;

	@FXML
	Button button;

	@FXML
	TextField textFieldURI;

	static final Font FONT_CATEGORY = Font.font("Arial", FontWeight.BOLD, 20);

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

	void addLog(TextArea textAreaLog, String msg) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		textAreaLog.setText("[" + sdf.format(timestamp) + "] " + msg + "\n" + textAreaLog.getText());
	}

	protected String getInteractionHref(JsonObject joInteraction, String protocol) {
		if (joInteraction.containsKey(JSONLD.KEY_FORMS) && joInteraction.get(JSONLD.KEY_FORMS).getValueType() == ValueType.ARRAY) {
			JsonArray jaForms = joInteraction.get(JSONLD.KEY_FORMS).asJsonArray();
			for(int i=0; i<jaForms.size(); i++) {
				// pick right form / mediaType
				if (jaForms.get(i) != null && jaForms.get(i).getValueType() == ValueType.OBJECT) {
					JsonObject joForm = jaForms.get(i).asJsonObject();

					if (joForm.containsKey(JSONLD.KEY_HREF) && joForm.get(JSONLD.KEY_HREF).getValueType() == ValueType.STRING) {
						String href = joForm.getString(JSONLD.KEY_HREF);
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

	protected void loadTD(final JsonObject jobj) {
		Tab t = new Tab(JSONLD.getThingName(jobj));

		TabPane tabPaneInner = new TabPane();
		tabPaneInner.setSide(Side.BOTTOM);
		tabPaneInner.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		t.setContent(tabPaneInner);

		List<ProtocolMediaType> protocols = JSONLD.getProtocols(jobj);
		for(ProtocolMediaType protocol : protocols) {

			Tab ti = new Tab(protocol.toString());
			
			tabPaneInner.getTabs().add(ti);

			GridPane gridPane = new GridPane();
			gridPane.setHgap(10);
			gridPane.setVgap(10);
			gridPane.setPadding(new Insets(0, 10, 0, 10));

			Window owner = borderPaneRoot.getScene().getWindow();
			// gridPane.setPrefSize(owner.getWidth(), owner.getHeight()); // Default
			// width and height
			gridPane.prefWidthProperty().bind(owner.widthProperty().subtract(25+15+15));
			// gridPane.setMaxSize(Region.USE_COMPUTED_SIZE,
			// Region.USE_COMPUTED_SIZE);

			gridPane.getColumnConstraints().add(new ColumnConstraints()); // 0
			gridPane.getColumnConstraints().add(new ColumnConstraints()); // 1
			ColumnConstraints column2 = new ColumnConstraints();
			column2.setHgrow(Priority.ALWAYS);
			gridPane.getColumnConstraints().add(column2); // 2

			final TextArea textAreaLog = new TextArea();
			textAreaLog.setEditable(false);

			int row = 0;
			// properties
			{
				JsonValue props = jobj.get(JSONLD.KEY_PROPERTIES);
				if (props != null && props.getValueType() == ValueType.OBJECT) {
					JsonObject joProps = props.asJsonObject();
					Set<String> keys = joProps.keySet();

					if (keys.size() > 0) {
						Text category = new Text("Properties:");
						category.setFont(FONT_CATEGORY);
						gridPane.add(category, 0, row++, 4, 1); // colidx, rowIdx,
																// colSpan, rowSpan
						for (final String propertyName : keys) {
							JsonValue jvProperty = joProps.get(propertyName);
							if (jvProperty != null && jvProperty.getValueType() == ValueType.OBJECT) {
								JsonObject joProperty = jvProperty.asJsonObject();

								Text textProp = new Text(propertyName + ":");
								gridPane.add(textProp, 1, row);
								GridPane.setHalignment(textProp, HPos.RIGHT);

								VBox vboxTextFields = new VBox();
								//
								final TextField textFieldGET = new TextField();
								textFieldGET.setEditable(false);
								textFieldGET.setDisable(true);
								vboxTextFields.getChildren().add(textFieldGET);

								//

								VBox vboxTextButtons = new VBox();
								String href = getInteractionHref(joProperty, protocol.protocol);

								if(href != null) {

									Button buttonGET = new Button("GET");
									buttonGET.setOnAction(new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent e) {
											LOGGER.info("GET " + propertyName);
											ClientFactory cf = new ClientFactory();
											try {


												URI uri = new URI(href);
												Client client = cf.getClient(uri);
												System.out.println(client);
												Callback callback = new AbstractCallback() {
													@Override
													public void onGet(final String propertyName, Content response) {
														final String res = new String(response.getContent());
														Platform.runLater(new Runnable() {
															@Override
															public void run() {
																textFieldGET.setText(res);
																String msg = "Success: GET of " + propertyName + ": " + res;
																addLog(textAreaLog, msg);
															}
														});
													}

													@Override
													public void onGetError(String propertyName) {
														String msg = "Error: GET of " + propertyName + " failed";
														LOGGER.warning(msg);
														addLog(textAreaLog, msg);
													}
												};
												client.get(propertyName, uri, callback);

											} catch (Exception e1) {
												// log error
												String msg = "Error: " + e1.getMessage();
												addLog(textAreaLog, msg);
												e1.printStackTrace();
											}
										}
									});

									HBox hboxTextButtons = new HBox();
									hboxTextButtons.getChildren().add(buttonGET);

									// observable
									if (joProperty.containsKey(JSONLD.KEY_OBSERVABLE)
											&& joProperty.get(JSONLD.KEY_OBSERVABLE).getValueType() == ValueType.TRUE) {
										ToggleButton tbObs = new ToggleButton("OBS");
										hboxTextButtons.getChildren().add(tbObs);
									}

									vboxTextButtons.getChildren().add(hboxTextButtons);

									// writable
									if (joProperty.containsKey(JSONLD.KEY_WRITABLE)
											&& joProperty.get(JSONLD.KEY_WRITABLE).getValueType() == ValueType.TRUE) {
										TextField textFieldPUT = new TextField();
										vboxTextFields.getChildren().add(textFieldPUT);

										//
										Button buttonPUT = new Button("PUT");
										buttonPUT.setOnAction(new EventHandler<ActionEvent>() {
											@Override
											public void handle(ActionEvent e) {
												LOGGER.info("PUT " + propertyName);

												ClientFactory cf = new ClientFactory();
												try {
													// String href = getInteractionHref(joProperty);

													URI uri = new URI(href);
													Client client = cf.getClient(uri);
													System.out.println(client);
													Callback callback = new AbstractCallback() {
														@Override
														public void onPut(final String propertyName, Content response) {
															final String res = new String(response.getContent());
															Platform.runLater(new Runnable() {
																@Override
																public void run() {
																	textFieldGET.setText(res);
																	String msg = "Success: PUT of " + propertyName + ": " + res;
																	addLog(textAreaLog, msg);
																}
															});
														}

														@Override
														public void onPutError(String propertyName, String message) {
															String msg = "Error: PUT of " + propertyName + " failed: "
																	+ message;
															LOGGER.warning(msg);
															addLog(textAreaLog, msg);
														}
													};
													// TODO mediaType
													Content propertyValue = new Content(textFieldPUT.getText().getBytes(),
															MediaType.APPLICATION_JSON);
													client.put(propertyName, uri, propertyValue, callback);
												} catch (Exception e1) {
													// log error
													String msg = "Error: " + e1.getMessage();
													addLog(textAreaLog, msg);
													e1.printStackTrace();
												}

											}
										});
										vboxTextButtons.getChildren().add(buttonPUT);
									}

									gridPane.add(vboxTextFields, 2, row);
									gridPane.add(vboxTextButtons, 3, row);

									row++;

								}
							} else {
								LOGGER.warning("Property value not object or null");
							}
						}
					}
				}
			}

			// actions
			{
				JsonValue actions = jobj.get(JSONLD.KEY_ACTIONS);
				if (actions != null && actions.getValueType() == ValueType.OBJECT) {
					JsonObject joActions = actions.asJsonObject();
					Set<String> keys = joActions.keySet();

					if (keys.size() > 0) {
						Text category = new Text("Actions:");
						category.setFont(FONT_CATEGORY);
						gridPane.add(category, 0, row++, 4, 1); // colidx, rowIdx,
																// colSpan, rowSpan
						for (final String actionName : keys) {
							JsonValue jvAction = joActions.get(actionName);
							if (jvAction != null && jvAction.getValueType() == ValueType.OBJECT) {
								JsonObject joAction = jvAction.asJsonObject();

								Text textProp = new Text(actionName + ":");
								gridPane.add(textProp, 1, row);
								GridPane.setHalignment(textProp, HPos.RIGHT);

								Button buttonPOST = new Button("POST");
								buttonPOST.setOnAction(new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent e) {
										LOGGER.info("POST " + actionName);

										ClientFactory cf = new ClientFactory();
										try {
											String href = getInteractionHref(joAction, protocol.protocol);
											// List<Form> forms =
											// JSONLD.getActionForms(jobj,
											// actionName);
											// if(forms.size() > 0) {
											// URI uri = new URI(forms.get(0).href);
											URI uri = new URI(href);
											Client client = cf.getClient(uri);
											System.out.println(client);
											Callback callback = new AbstractCallback() {

												@Override
												public void onAction(String actionName, Content response) {
													String res = new String(response.getContent());
													// TODO response
													// textFieldPOST.setText(res);
													String msg = "Success: POST of " + actionName + ": " + res;
													addLog(textAreaLog, msg);
												}

												@Override
												public void onActionError(String actionName) {
													String msg = "Error: POST of " + actionName + " failed";
													LOGGER.warning(msg);
													addLog(textAreaLog, msg);
												}
											};
											// TODO action value
											Content actionValue = new Content(new byte[0], MediaType.APPLICATION_JSON);
											client.action(actionName, uri, actionValue, callback);
											// }
										} catch (Exception e1) {
											// log error
											String msg = "Error: " + e1.getMessage();
											addLog(textAreaLog, msg);
											e1.printStackTrace();
										}

									}
								});

								gridPane.add(buttonPOST, 3, row);

								row++;
							}
						}
					}
				}
			}

			// // events
			// {
			// List<String> events = JSONLD.getEvents(jobj);
			// if(events != null && events.size() > 0) {
			// Text category = new Text("Events:");
			// category.setFont(FONT_CATEGORY);
			// gridPane.add(category, 0, row++, 4, 1); // colidx, rowIdx, colSpan,
			// rowSpan
			// for(String ev : events) {
			// Text textProp = new Text(ev + ":");
			// gridPane.add(textProp, 1, row++);
			// GridPane.setHalignment(textProp, HPos.RIGHT);
			// }
			// }
			// }

			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setContent(gridPane);
			scrollPane.setPadding(new Insets(15, 15, 15, 15));

			SplitPane splitPane = new SplitPane();
			splitPane.setOrientation(Orientation.VERTICAL);
			splitPane.setDividerPositions(0.7);
			splitPane.getItems().add(scrollPane);

			splitPane.getItems().add(textAreaLog);
			// BorderPane bp = new BorderPane();
			// bp.setCenter(scrollPane);
			// bp.setBottom(new TextArea());
			// 
			
			
			ti.setContent(splitPane);
		}





		// t.setContent(splitPane);
		tabPane.getTabs().add(t);
		tabPane.getSelectionModel().select(t);
	}

//	@FXML
//	protected void handleMenuOpenTDURI(ActionEvent event) {
//		try {
//			// http://code.makery.ch/blog/javafx-8-dialogs/
//			// Dialogs.create()
//			// .owner(stage)
//			// .title("Information Dialog")
//			// .masthead("Look, an Information Dialog")
//			// .message("I have a great message for you!")
//			// .showInformation();
//			//
//			// ExceptionDialog ed = new ExceptionDialog(new
//			// RuntimeException("XX"));
//			// ed.show();
//
//			TextInputDialog dialog = new TextInputDialog("http://localhost:8080/counter");
//			dialog.setTitle("URI Input Dialog");
//			dialog.setHeaderText("Input Dialog for URI");
//			dialog.setContentText("Please enter URI:");
//
//			// Traditional way to get the response value.
//			Optional<String> result = dialog.showAndWait();
//			if (result.isPresent()) {
//				URI uri = new URI(result.get());
//				// TODO coap scheme use californium
//
//				URL url = uri.toURL(); // get URL from your uri object
//				try(InputStream istream = url.openStream()) {
//					JsonObject jobj = JSONLD.parseJSON(istream);
//					loadTD(jobj);
//				}
//			}
//
//			// // Note: jpro does not allow showAndWaits etc
//			// // https://www.jpro.one/?page=docs/current/1.8/JPRO%20CHECKLIST
//			// dialog.show();
//			//
//			// dialog.resultProperty().addListener(new ChangeListener<String>()
//			// {
//			// @Override
//			// public void changed(ObservableValue<? extends String> observable,
//			// String oldValue, String newValue) {
//			// try {
//			// LOGGER.info("changed to: " + newValue);
//			// URI uri = new URI(newValue);
//			// // TODO coap scheme use californium
//			//
//			// URL url = uri.toURL(); //get URL from your uri object
//			// InputStream istream = url.openStream();
//			//
//			// JsonObject jobj = JSONLD.parseJSON(istream);
//			// loadTD(jobj);
//			// } catch (Exception e) {
//			// LOGGER.severe(e.getMessage());
//			// showAlertDialog(e);
//			// }
//			// }
//			// });
//
//		} catch (Exception e) {
//			LOGGER.severe(e.getMessage());
//			showAlertDialog(e);
//		}
//	}

	@FXML
	protected void handleLoadTDURI(ActionEvent event) {
		try {
			URI uri = new URI(textFieldURI.getText());
			// TODO coap scheme use californium

			URL url = uri.toURL(); // get URL from your uri object
			try(InputStream istream = url.openStream()) {
				JsonObject jobj = JSONLD.parseJSON(istream);
				loadTD(jobj);
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			showAlertDialog(e);
		}
	}

//	@FXML
//	protected void handleMenuOpenTDFile(ActionEvent event) {
//		try {
//			Window owner = borderPaneRoot.getScene().getWindow();
//			FileChooser fc = new FileChooser();
//			File f = fc.showOpenDialog(owner);
//			if (f != null) {
//				LOGGER.info("Load file: " + f);
//				try(FileInputStream fis = new FileInputStream(f)) {
//					JsonObject jobj = JSONLD.parseJSON(fis);
//					loadTD(jobj);
//				}
//			}
//		} catch (Exception e) {
//			LOGGER.severe(e.getMessage());
//			showAlertDialog(e);
//		}
//	}

	public void showAlertDialog(Exception e) {
		showAlertDialog(e, "");
	}

	// http://code.makery.ch/blog/javafx-dialogs-official/
	public void showAlertDialog(Exception e, String msg) {
		boolean lightweightDialogs = true;

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

		if (lightweightDialogs) {
			JFXDialog jfxdialog = new JFXDialog();

			JFXDialogLayout layout = new JFXDialogLayout();
			layout.setHeading(new Label("Exception " + (msg == null ? "" : msg)));
			layout.setBody(expContent);
			JFXButton closeButton = new JFXButton("OK");
			// closeButton.getStyleClass().add("dialog-accept");
			closeButton.setOnAction(event -> jfxdialog.close());
			layout.setActions(closeButton);
			jfxdialog.setContent(layout);
			jfxdialog.show(stackPaneRoot);

		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Exception Dialog");
			alert.setHeaderText("Exception " + (msg == null ? "" : msg));
			alert.setContentText(e.getMessage());

			// Set expandable Exception into the dialog pane.
			alert.getDialogPane().setExpandableContent(expContent);

			alert.showAndWait();
		}
	}

//	@FXML
//	protected void handleMenuClose(ActionEvent event) {
//		System.exit(0);
//	}
}
