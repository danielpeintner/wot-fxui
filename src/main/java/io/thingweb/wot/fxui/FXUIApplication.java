package io.thingweb.wot.fxui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
//import org.controlsfx.tools.Borders;

import java.io.IOException;

public class FXUIApplication extends Application {

	   @Override
	    public void start(Stage primaryStage) throws Exception{
	        Parent root = FXMLLoader.load(getClass().getResource("MainLayout.fxml"));
	        primaryStage.setTitle("WoT-FX-UI Application");
	        primaryStage.setScene(new Scene(root, 800, 500));
	        primaryStage.show();
	    }


	    public static void main(String[] args) {
	        launch(args);
	    }
}