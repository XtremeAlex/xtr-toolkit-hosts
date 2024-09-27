package com.xtremealex.toolkit.hosts;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
public class MainHostsApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Font.loadFont(getClass().getResourceAsStream("/fonts/Overpass/Overpass-Light.ttf"), 14);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 640, 860);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        //scene.setFill(Color.BLACK);

        primaryStage.setTitle("XTR HOST");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
