package com.xtremealex.toolkit.hosts;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainClassDefinitivo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Simula il caricamento dei dati (App con Hosts e FQDN)
        List<App> apps = HostParser.parseHostsFile("/etc/hosts");

        // Layout principale
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Font per il testo
        Font font = Font.font("Arial", FontWeight.NORMAL, 14);

        for (App app : apps) {
            VBox appContainer = new VBox(10);  // Contenitore per ogni App

            // Se l'App è un bilanciatore, aggiungi il pulsante "Aggiorna" prima del titolo
            if (app.getHostType() == HostType.BALANCER) {
                Button refreshButton = new Button("Aggiorna");
                refreshButton.setStyle("-fx-background-color: lightblue;");
                refreshButton.setOnAction(e -> {
                    // Logica di aggiornamento da integrare
                    System.out.println("Aggiornamento IP per " + app.getLb());
                });
                appContainer.getChildren().add(refreshButton);  // Aggiungi il pulsante prima del titolo
            }

            // Titolo per ogni App
            Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
            appLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            appLabel.setStyle("-fx-padding: 10; -fx-background-color: lightgray; -fx-border-color: gray; -fx-border-width: 1;");
            appContainer.getChildren().add(appLabel);

            // Raggruppiamo gli IP unici e i FQDN corrispondenti, separando ON e OFF
            Map<String, List<Host>> ipToHostMap = new HashMap<>();
            for (Host host : app.getHosts()) {
                String ip = host.getIp();
                ipToHostMap.putIfAbsent(ip, new ArrayList<>());
                ipToHostMap.get(ip).add(host);
            }

            // Aggiungi i dati IP e FQDN raggruppati, prima ON e poi OFF
            for (String ip : ipToHostMap.keySet()) {
                List<Host> hosts = ipToHostMap.get(ip);

                HBox hostBox = new HBox(10); // Layout orizzontale per ogni host
                hostBox.setAlignment(Pos.CENTER_LEFT);

                // Etichetta per IP (mostrato una sola volta per ciascun gruppo)
                Label ipLabel = new Label(ip);
                ipLabel.setFont(font);

                // VBox per i FQDN associati all'IP
                VBox fqdnBox = new VBox(5);
                fqdnBox.setPadding(new Insets(5, 0, 0, 10)); // Spaziatura tra i FQDN

                // Prima mostriamo i FQDN ON
                for (Host host : hosts) {
                    if (host.isEnabled()) {
                        Label fqdnLabel = new Label(host.getFqdn());
                        fqdnLabel.setFont(font);
                        fqdnBox.getChildren().add(fqdnLabel);
                    }
                }

                // Poi mostriamo i FQDN OFF
                for (Host host : hosts) {
                    if (!host.isEnabled()) {
                        Label fqdnLabel = new Label(host.getFqdn());
                        fqdnLabel.setFont(font);
                        fqdnBox.getChildren().add(fqdnLabel);
                    }
                }

                // Aggiungi toggle per ON/OFF con logica unificata per tutto il gruppo IP
                boolean hasEnabledHosts = hosts.stream().anyMatch(Host::isEnabled);
                ToggleButton toggleButton = new ToggleButton(hasEnabledHosts ? "ON" : "OFF");
                toggleButton.setSelected(hasEnabledHosts);
                toggleButton.setStyle(hasEnabledHosts ? "-fx-background-color: lightgreen;" : "-fx-background-color: lightcoral;");
                toggleButton.setOnAction(e -> {
                    boolean newStatus = toggleButton.isSelected();
                    toggleButton.setText(newStatus ? "ON" : "OFF");
                    toggleButton.setStyle(newStatus ? "-fx-background-color: lightgreen;" : "-fx-background-color: lightcoral;");
                    // Aggiorna lo stato di tutti i FQDN associati a questo IP
                    hosts.forEach(host -> host.setEnabled(newStatus));
                });

                // Aggiungi al layout orizzontale
                hostBox.getChildren().addAll(toggleButton, ipLabel, fqdnBox);
                appContainer.getChildren().add(hostBox);  // Aggiungi l'hostBox al container dell'app
            }

            // Aggiungi l'app completa al layout principale
            mainLayout.getChildren().add(appContainer);
        }

        // Creiamo uno ScrollPane per gestire lo scorrimento
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);

        // Layout della scena principale
        Scene scene = new Scene(scrollPane, 600, 400);
        primaryStage.setTitle("Gestione Hosts");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
