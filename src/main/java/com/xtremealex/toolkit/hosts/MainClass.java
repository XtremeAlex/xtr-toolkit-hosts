package com.xtremealex.toolkit.hosts;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.Parent;
public class MainClass extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Font.loadFont(getClass().getResourceAsStream("/fonts/Overpass-Regular.ttf"), 14);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 640, 860);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        scene.setFill(Color.BLACK);

        primaryStage.setTitle("XTR HOST");
        primaryStage.setScene(scene);
        primaryStage.show();

        //PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        //delay.setOnFinished(event -> primaryStage.show());
        //delay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
