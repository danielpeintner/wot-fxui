package io.thingweb.wot.fxui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.thingweb.wot.fxui.JSONLD.Form;
import io.thingweb.wot.fxui.JSONLD.ProtocolMediaType;
import io.thingweb.wot.fxui.JSONLD.SecurityScheme;
import io.thingweb.wot.fxui.client.*;
import io.thingweb.wot.fxui.client.Client.RequestOption;
import io.thingweb.wot.fxui.client.impl.AbstractCallback;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Window;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;

import javax.json.*;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

public class MainLayoutController implements Initializable {

	private final static Logger LOGGER = Logger.getLogger(MainLayoutController.class.getName());

	@FXML
	StackPane stackPaneRoot;

	@FXML
	BorderPane borderPaneRoot;

	@FXML
	TabPane tabPane;

	@FXML
	TextField textFieldURI;

	@FXML
	TextField textFieldDirectory;

	@FXML
	VBox vBoxDirectory;

	@FXML
	TextArea textAreaJSONLD;

	@FXML
	Button buttonHypermediaControl;

	@FXML
	ComboBox comboBoxHypermediaControl;

	@FXML
	Button buttonInvokeFade;

	@FXML
	Label labelTD;

	static final Font FONT_CATEGORY = Font.font("Arial", FontWeight.BOLD, 20);

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

	void addLog(TextArea textAreaLog, String msg) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		textAreaLog.setText("[" + sdf.format(timestamp) + "] " + msg + "\n" + textAreaLog.getText());
	}


	private List<RequestOption> getRequestOptions(ComboBox<String> comboBoxSecurity, TextField textFieldUsername, TextField textFieldPassword) {
		List<RequestOption> requestOptions = new ArrayList<>();

		if(comboBoxSecurity.getSelectionModel().getSelectedIndex() == 1) {
			String username = textFieldUsername.getText().trim();
			String password = textFieldPassword.getText().trim();
			String userpass = username + ":" + password;
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));

			RequestOption ro1 = new RequestOption("Authorization", -1, basicAuth);

			requestOptions.add(ro1);
		}

		return requestOptions;
	}

	static class PropertyForm {
		public final Form form;
		public final boolean readOnly;
		public final boolean writeOnly;
		public final boolean observable;
		public PropertyForm(Form form, boolean readOnly, boolean writeOnly, boolean observable) {
			this.form = form;
			this.readOnly = readOnly;
			this.writeOnly = writeOnly;
			this.observable = observable;
		}
	}

	protected void loadTD(final JsonObject jobj) {
		Tab t = new Tab(JSONLD.getThingTitle(jobj));

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

			String base = JSONLD.getBase(jobj);

			final TextField textFieldUsername = new TextField();
			final PasswordField passwordFieldPassword = new PasswordField();

			ObservableList<String> securitySchemes =
				    FXCollections.observableArrayList(
				        "nosec",
				        "basic"
				    );

			ComboBox<String> comboBoxSecurity = new ComboBox<>(securitySchemes);
			comboBoxSecurity.getSelectionModel().select(0);

			// Add information about security
			List<SecurityScheme> ss = JSONLD.getSecuritySchemes(jobj);
			if(ss.size() > 1 || (ss.size() >= 1 && !ss.contains(SecurityScheme.nosec))){
				Text category = new Text("Security:");
				category.setFont(FONT_CATEGORY);
				gridPane.add(category, 0, row++, 4, 1); // colidx, rowIdx,
				// colSpan, rowSpan

				gridPane.add(new Label("Scheme: "), 0, row, 1, 1);
				gridPane.add(comboBoxSecurity, 1, row, 3, 1);
				row++;

				gridPane.add(new Label("Username: "), 0, row, 1, 1);
				gridPane.add(textFieldUsername, 1, row, 3, 1);
				row++;

				gridPane.add(new Label("Password: "), 0, row, 1, 1);
				gridPane.add(passwordFieldPassword, 1, row, 3, 1);
				row++;
			}

			// global forms
			Form formGlobal = JSONLD.getInteractionForm(jobj, base, protocol.protocol);

			// properties
			Map<String, JsonObject> properties = JSONLD.getProperties(jobj);
			if(properties.size() > 0 || formGlobal != null) {

				Text category = new Text("Properties:");
				category.setFont(FONT_CATEGORY);
				gridPane.add(category, 0, row++, 4, 1); // colidx, rowIdx,
														// colSpan, rowSpan


				Map<String, PropertyForm> props = new HashMap<>();
				if(formGlobal != null) {
					props.put("#all", new PropertyForm(formGlobal, false, false, false));
				}
				for(String propertyName : properties.keySet()) {
					JsonObject joProperty = properties.get(propertyName);
					Form form = JSONLD.getInteractionForm(joProperty, base, protocol.protocol);
					boolean readOnly = false;
					boolean writeOnly = false;
					boolean observable = false;
					if (joProperty.containsKey(JSONLD.KEY_READONLY)
							&& joProperty.get(JSONLD.KEY_READONLY).getValueType() == ValueType.TRUE) {
						readOnly = true;
					}
					if (joProperty.containsKey(JSONLD.KEY_WRITEONLY)
							&& joProperty.get(JSONLD.KEY_WRITEONLY).getValueType() == ValueType.TRUE) {
						writeOnly = true;
					}
					if (joProperty.containsKey(JSONLD.KEY_OBSERVABLE)
							&& joProperty.get(JSONLD.KEY_OBSERVABLE).getValueType() == ValueType.TRUE) {
						observable = true;
					}
					props.put(propertyName, new PropertyForm(form, readOnly, writeOnly, observable));
				}

				// for(String propertyName : properties.keySet()) {
				for(String propertyName : props.keySet()) {
					// JsonObject joProperty = properties.get(propertyName);
					// Form form = JSONLD.getInteractionForm(joProperty, base, protocol.protocol);
					PropertyForm propForm = props.get(propertyName);

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

					if(propForm.form != null && propForm.form.href != null) {
						Button buttonGET = new Button(); // "GET"
						buttonGET.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EYE)); // BINOCULARS
						buttonGET.setTooltip(new Tooltip(propForm.form.href));
						buttonGET.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent e) {
								LOGGER.info("GET " + propertyName);
								ClientFactory cf = new ClientFactory();
								try {
									URI uri = new URI(getUriVariableSanitized(propForm.form.href));
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


									client.get(propertyName, uri, callback, getRequestOptions(comboBoxSecurity, textFieldUsername, passwordFieldPassword));

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
						if (propForm.observable) {
							ToggleButton tbObs = new ToggleButton("OBS");
							hboxTextButtons.getChildren().add(tbObs);
						}

						vboxTextButtons.getChildren().add(hboxTextButtons);

						// writable
						if (!propForm.readOnly) {
							TextField textFieldPUT = new TextField();
							vboxTextFields.getChildren().add(textFieldPUT);

							//
							Button buttonPUT = new Button(); // "PUT"
							buttonPUT.setTooltip(new Tooltip(propForm.form.href));
							buttonPUT.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EDIT));
							buttonPUT.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent e) {
									LOGGER.info("PUT " + propertyName);

									ClientFactory cf = new ClientFactory();
									try {
										// String href = getInteractionHref(joProperty);

										URI uri = new URI(getUriVariableSanitized(propForm.form.href));
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
										// mediaType
										Content propertyValue = new Content(textFieldPUT.getText().getBytes(), MediaType.getMediaType(propForm.form.mediaType));
										client.put(propertyName, uri, propertyValue, callback, getRequestOptions(comboBoxSecurity, textFieldUsername, passwordFieldPassword));
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
				}
			}

			// actions
			Map<String, JsonObject> actions = JSONLD.getActions(jobj);
			if(actions.size() > 0) {
				Text category = new Text("Actions:");
				category.setFont(FONT_CATEGORY);
				gridPane.add(category, 0, row++, 4, 1); // colidx, rowIdx,
														// colSpan, rowSpan

				for(String actionName : actions.keySet()) {
					JsonObject joAction = actions.get(actionName);

					Text textProp = new Text(actionName + ":");
					gridPane.add(textProp, 1, row);
					GridPane.setHalignment(textProp, HPos.RIGHT);

					Form form = JSONLD.getInteractionForm(joAction, base, protocol.protocol);

					Button buttonPOST = new Button(); // "POST"
					buttonPOST.setTooltip(new Tooltip(form.href));
					buttonPOST.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLAY));
					buttonPOST.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent e) {
							LOGGER.info("POST " + actionName);

							ClientFactory cf = new ClientFactory();
							try {

								// List<Form> forms =
								// JSONLD.getActionForms(jobj,
								// actionName);
								// if(forms.size() > 0) {
								// URI uri = new URI(forms.get(0).href);
								URI uri = new URI(getUriVariableSanitized(form.href));
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
								client.action(actionName, uri, actionValue, callback, getRequestOptions(comboBoxSecurity, textFieldUsername, passwordFieldPassword));
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
			scrollPane.setPadding(new Insets(10, 10, 10, 10));

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

	// e.g., "http://plugfest.thingweb.io:8083/counter/actions/increment{?step}" --> "http://plugfest.thingweb.io:8083/counter/actions/increment"
	String getUriVariableSanitized(String href) {
		if (href != null) {
			int indexBegin = href.indexOf("{?");
			if(indexBegin > 0) {
				return href.substring(0, indexBegin);
				// Question: Can there be multiple uriVariables and in the middle of an URL?
				/*
				int indexEnd = href.indexOf("}", indexBegin);
				if(indexEnd > 0) {
					//
				}
				*/
			}
		}
		return href;
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
			JsonObject td;
			String suri = textFieldURI.getText();
			if (suri != null) {
				suri = suri.trim();
				if (suri.startsWith("coap")) {
					CoapClient coap = new CoapClient(suri);
					CoapResponse response = coap.get();
					td = JSONLD.parseJSON(response.getResponseText());
				} else {
					URI uri = new URI(suri);
					URL url = uri.toURL(); // get URL from your uri object
					try(InputStream istream = url.openStream()) {
						td = JSONLD.parseJSON(istream);
					}
				}
				loadTD(td);
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			showAlertDialog(e);
		}
	}

	@FXML
	protected void handleLoadTD(ActionEvent event) {
		try {
			String sJsonLD = textAreaJSONLD.getText();

			JsonObject jobj = JSONLD.parseJSON(sJsonLD);
			loadTD(jobj);

		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			showAlertDialog(e);
		}
	}

	@FXML
	protected void handleLoadTDDirectory(ActionEvent event) {
		try {
			URI uri = new URI(textFieldDirectory.getText());
			// TODO coap scheme use californium

			URL url = uri.toURL(); // get URL from your uri object
			try(InputStream istream = url.openStream()) {
				JsonObject jobj = JSONLD.parseJSON(istream);

				vBoxDirectory.getChildren().clear();

				LocalDate ld = LocalDate.now();
				LocalTime lt = LocalTime.now();
				Label l = new Label("\nThe following TDs have been found (" + ld + " " + lt + ")");
				vBoxDirectory.getChildren().add(l);

				for(String key : jobj.keySet()) {
					Button b = new Button("Load \"" + key +"\"");
					b.setOnAction(new EventHandler<ActionEvent>() {
					    @Override public void handle(ActionEvent e) {
					    	loadTD(jobj.getJsonObject(key));
					    }
					});

					vBoxDirectory.getChildren().add(b);
				}
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			showAlertDialog(e);
		}
	}

	final int PORT = 8084;
	final String thingName = "/HypermediaControlThing";
	final String actionName = "/fade";
	final String actionNameId = "/fade/1";

	static HttpServer server = null;
	static String ip;

	boolean fadeActionRunning = false;
	FadeTimerTask fadeTimerTask;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			// Just in case this host has multiple IP addresses....
			List<InetAddress> ias = getListOfIPsFromNIs();
			for(InetAddress ia : ias) {
				comboBoxHypermediaControl.getItems().add(ia.getHostAddress());
			}
			comboBoxHypermediaControl.getSelectionModel().select(0);
		} catch (SocketException e) {
			// ignore
		}
	}

	@FXML
	// https://github.com/w3c/wot-thing-description/tree/master/proposals/hypermedia-control
	protected void startHypermediaControlServer(ActionEvent event) {
		try {
			// https://github.com/w3c/wot-thing-description/tree/master/proposals/hypermedia-control

			if(server == null) {
				// InetAddress inetAddress = InetAddress.getLocalHost();
				// ip = inetAddress.getHostAddress();
				ip = comboBoxHypermediaControl.getSelectionModel().getSelectedItem().toString();
				System.out.println("IP Address:- " + ip);
				// System.out.println("Host Name:- " + inetAddress.getHostName());

				server = HttpServer.create(new InetSocketAddress(ip, PORT), 0);

				server.createContext(thingName, new TDHandler());
				server.createContext(thingName + actionName, new ActionHandler());
				server.setExecutor(null); // creates a default executor
				server.start();

				// buttonHypermediaControl.setDisable(true);
				buttonHypermediaControl.setText("Stop Hypermedia-Control Server");
				comboBoxHypermediaControl.setDisable(true);
				buttonInvokeFade.setDisable(false);
				labelTD.setText("TD --> " + getURL(null));
				labelTD.setVisible(true);
				labelTD.setManaged(true);
			} else {
				// stop
				server.stop(0); // no delay
				server = null;

				// buttonHypermediaControl.setDisable(false);
				buttonHypermediaControl.setText("Start Hypermedia-Control Server");
				comboBoxHypermediaControl.setDisable(false);
				buttonInvokeFade.setDisable(true);
				labelTD.setVisible(false);
				labelTD.setManaged(false);
			}

		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			showAlertDialog(e);
		}
	}

	// Get list of IP addresses from all local network interfaces
	public List<InetAddress> getListOfIPsFromNIs() throws SocketException {
		List<InetAddress> addrList = new ArrayList<>();
		Enumeration<NetworkInterface> enumNI = NetworkInterface.getNetworkInterfaces();
		while (enumNI.hasMoreElements()) {
			NetworkInterface ifc = enumNI.nextElement();
			if (ifc.isUp()) {
				Enumeration<InetAddress> enumAdds = ifc.getInetAddresses();
				while (enumAdds.hasMoreElements()) {
					InetAddress addr = enumAdds.nextElement();
					addrList.add(addr);
				}
			}
		}
		return addrList;
	}

	@FXML
	protected void showTD() {
		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
				new Thread(() -> {
					try {
						Desktop.getDesktop().browse(new URI(getURL(null)));
					} catch (IOException | URISyntaxException e) {
						e.printStackTrace();
					}
				}).start();
			}
		} catch (Exception e) {
			// noop
		}
	}

	@FXML
	protected void invokeFade() {
		try {
			if(!fadeActionRunning) {
				server.createContext(thingName + actionNameId, new ActionRunningHandler());
				// start task
				long delayMs = 0 * 1000; // 0 seconds
				long periodMs = (1000) * 1; // X seconds
				long runtimeMs = (1000) * 30; // X seconds
				Timer timer = new Timer();
				fadeTimerTask = new FadeTimerTask(server, buttonInvokeFade, runtimeMs);
				timer.schedule(fadeTimerTask, delayMs, periodMs);
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			// showAlertDialog(e);
		}
	}

	protected void stopFade() {
		try {
			if(fadeActionRunning) {
				fadeActionRunning = false;
				server.removeContext(thingName + actionNameId);
				fadeTimerTask.cancel();
				Platform.runLater(()->{
					fadeTimerTask.buttonInvoke.setDisable(false);
					fadeTimerTask.buttonInvoke.setText(fadeTimerTask.originalText);
					fadeTimerTask = null;
				});
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
		}
	}

	String getURL(String app) {
		if(app == null) {
			return "http://" + ip + ":" + PORT+ thingName;
		} else {
			return "http://" + ip + ":" + PORT + thingName + app;
		}
	}

	JsonObject createTD() {
		JsonArrayBuilder formsBuilder = Json.createArrayBuilder();
		// single form always
		formsBuilder.add(Json.createObjectBuilder()
				.add("href", getURL(actionName))
				.add("htv:methodName", "POST")
				.add("op", "invokeaction")
		);
		if(fadeActionRunning) {
			// add more forms / controls
			formsBuilder.add(Json.createObjectBuilder()
					.add("href", getURL(actionNameId))
					.add("htv:methodName", "GET")
					.add("op", "readaction")
			);
			// need for PUT ?
//			formsBuilder.add(Json.createObjectBuilder()
//					.add("href", "http://" + ip + ":" + PORT + thingName + actionNameId)
//					.add("htv:methodName", "PUT")
//			);
			formsBuilder.add(Json.createObjectBuilder()
					.add("href", getURL(actionNameId))
					.add("htv:methodName", "DELETE")
					.add("op", "cancelaction")
			);
		}


		JsonObject td = Json.createObjectBuilder()
				.add("@context", "https://www.w3.org/2019/wot/td/v1")
				.add("title", "HypermediaControlThing")
				.add("id", "urn:com:siemens:HypermediaControlThing")
				.add("securityDefinitions", Json.createObjectBuilder()
						.add("nosec_sc", Json.createObjectBuilder()
								.add("scheme", "nosec")
						)
				)
				.add("security", "nosec_sc")
				.add("actions", Json.createObjectBuilder()
						.add("fade", Json.createObjectBuilder()
								.add("forms", formsBuilder
								)
						)
				)
				.build();
		return td;
	}

	String getJsonString(JsonObject jo) {
		Map<String, Boolean> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory jwf = Json.createWriterFactory(config);
		StringWriter sw = new StringWriter();
		try (JsonWriter jsonWriter = jwf.createWriter(sw)) {
			jsonWriter.writeObject(jo);
		}
		return sw.toString();
	}


	class TDHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			// otherwise the handler would also handle /fade/XXX
			if(t.getRequestURI().getPath().endsWith(thingName)) {
				JsonObject td = createTD();
				String response = getJsonString(td);
				reportJson(t, response);
			} else {
				report404(t);
			}
		}
	}

	void reportJson(HttpExchange t, String response) throws IOException {
		response = response == null ? "" : response;
		t.getResponseHeaders().set("content-type", "application/json");
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	void report404(HttpExchange t) throws IOException {
		String response = "404";
		t.sendResponseHeaders(404, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	class ActionHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			if("POST".equals(t.getRequestMethod())) {
				// start action
				invokeFade();
				reportJson(t, null);
			} else {
				report404(t);
			}
		}
	}

	class ActionRunningHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			JsonObject object = null;
			switch(t.getRequestMethod()) {
				case "GET":
					if(fadeActionRunning) {
						// report remaining running time
						object = Json.createObjectBuilder()
								.add("fadeRemaining", fadeTimerTask.runtimeRemaining)
								.build();
					}
					break;
				case "PUT":
					// what to do?
					break;
				case "DELETE":
					if(fadeActionRunning) {
						// stop action & report remaining running time being 0
						object = Json.createObjectBuilder()
								.add("fadeRemaining", 0)
								.build();
						stopFade();
					}
					break;
			}
			if(object != null) {
				String response = getJsonString(object);
				reportJson(t, response);
			} else {
				report404(t);
			}
		}
	}

	class FadeTimerTask extends TimerTask {

		// milliseconds
		long lastRun = 0;
		final HttpServer server;
		final Button buttonInvoke;
		long runtimeRemaining;
		String originalText;

		public FadeTimerTask(HttpServer server, Button buttonInvoke, long runtime) {
			this.server = server;
			this.buttonInvoke = buttonInvoke;
			this.runtimeRemaining = runtime;
		}

		@Override
		public void run() {
			fadeActionRunning = true;
			long currentMillis = System.currentTimeMillis();
			if(lastRun == 0) {
				// first run
				Platform.runLater(()->{
					this.buttonInvoke.setDisable(true);
					originalText = this.buttonInvoke.getText();
					this.buttonInvoke.setText(actionName + " vs. " + actionNameId + " (Running for " + runtimeRemaining + " ms ...");
				});
			} else {
				// calculate remaining
				runtimeRemaining -= (currentMillis - lastRun); // milliseconds
				if (runtimeRemaining > 0) {
					// continue
					Platform.runLater(()->{
						this.buttonInvoke.setDisable(true);
						this.buttonInvoke.setText(actionName + " vs. " + actionNameId + " (Running for " + runtimeRemaining + " ms ...");
					});
				} else {
					// stop
					stopFade();
				}
			}
			lastRun = currentMillis;
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
