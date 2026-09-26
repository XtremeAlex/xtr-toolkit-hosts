package old;

import com.jfoenix.controls.JFXButton;
import com.xtremealex.toolkit.hosts.models.App;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MainClassBis extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Simula il caricamento dei dati (App con Hosts e FQDN)
        List<App> apps = HostParser.parseHostsFile("/etc/hosts");

        VBox mainLayout = new VBox(20); // Layout principale con spazio tra le APP
        mainLayout.setStyle("-fx-padding: 20px;"); // Padding per tutto il layout

        // Simulazione di una lista di APPs
        for (int appIndex = 0; appIndex < 3; appIndex++) {
            VBox appBox = new VBox(10); // Layout per ogni APP con spaziatura
            appBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #dcdcdc; -fx-border-radius: 10px; -fx-padding: 15px;");

            // Label per il nome dell'APP
            Label appLabel = new Label("APP " + (appIndex + 1));
            appLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            // Simulazione di IP e FQDNs
            for (int ipIndex = 0; ipIndex < 2; ipIndex++) {
                HBox ipBox = new HBox(10); // Box orizzontale per IP e FQDN
                ipBox.setStyle("-fx-alignment: center-left;");

                // Bottone ON/OFF
                JFXButton onOffButton = new JFXButton(ipIndex % 2 == 0 ? "ON" : "OFF");
                onOffButton.setStyle(ipIndex % 2 == 0 ? "-fx-background-color: #98FB98;" : "-fx-background-color: #FF6347;");
                onOffButton.setPrefSize(50, 30);

                // Label per l'IP
                Label ipLabel = new Label("127.0.0." + ipIndex);
                ipLabel.setStyle("-fx-font-size: 16px;");

                VBox fqdnBox = new VBox(5); // Box verticale per i FQDN
                fqdnBox.setStyle("-fx-padding: 0 0 0 30px;"); // Indentazione per i FQDN

                // Aggiungi i FQDN associati all'IP
                for (int fqdnIndex = 0; fqdnIndex < 3; fqdnIndex++) {
                    Label fqdnLabel = new Label("fqdn" + fqdnIndex + ".com");
                    fqdnLabel.setStyle("-fx-font-size: 14px;");
                    fqdnBox.getChildren().add(fqdnLabel);
                }

                // Aggiungi i componenti nell'IP Box
                ipBox.getChildren().addAll(onOffButton, ipLabel, fqdnBox);
                appBox.getChildren().add(ipBox);
            }

            // Pulsante di aggiornamento
            JFXButton updateButton = new JFXButton("Aggiorna");
            updateButton.setStyle("-fx-background-color: #ADD8E6; -fx-text-fill: #000000;");
            updateButton.setOnAction(e -> {
                // Azione di aggiornamento futura
                System.out.println("Aggiorna IP");
            });

            appBox.getChildren().addAll(appLabel, updateButton); // Aggiungi i componenti al layout dell'APP
            mainLayout.getChildren().add(appBox);
        }

        // Configurazione e visualizzazione della finestra
        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setTitle("Gestione Hosts");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
