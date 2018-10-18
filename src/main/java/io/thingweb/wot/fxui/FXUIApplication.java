package io.thingweb.wot.fxui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXUIApplication extends Application {

	   @Override
	    public void start(Stage primaryStage) throws Exception{
		   	// NOTE: NEEDS TO COME FIRST!!
			// Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
		   
	        Parent root = FXMLLoader.load(getClass().getResource("MainLayout.fxml"));
	        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
	        
	        // DeviceDetection
			if(DeviceDetection.isAndroid()) {
				primaryStage.setScene(new Scene(root, bounds.getWidth(), bounds.getHeight()));
			} else {
				// desktop
				primaryStage.setScene(new Scene(root, 800, 600));
			}
	        
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			    @Override
			    public void handle(WindowEvent t) {
			        Platform.exit();
			        System.exit(0);
			    }
			});
			
	        primaryStage.setTitle("WoT-FX-UI Application");
	        primaryStage.show();
	    }


	    public static void main(String[] args) {
	        launch(args);
	    }
}