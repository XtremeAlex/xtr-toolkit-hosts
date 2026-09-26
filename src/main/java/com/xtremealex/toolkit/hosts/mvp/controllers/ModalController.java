package com.xtremealex.toolkit.hosts.mvp.controllers;

import com.jfoenix.controls.JFXButton;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;
import com.xtremealex.toolkit.hosts.mvp.views.impl.MainPresenterImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModalController {

    private final MainPresenterImpl presenter;

    public ModalController(MainPresenterImpl presenter) {
        this.presenter = presenter;
    }

    /**
     * Apre una finestra modale per aggiungere una nuova App.
     */
    public void openAddAppModal() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Aggiungi App");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));
        dialogVBox.setAlignment(Pos.CENTER);

        // Campo per il nome dell'App
        TextField appNameField = new TextField();
        appNameField.setPromptText("Nome App");

        // Campo per le informazioni dell'App
        TextField appInfoField = new TextField();
        appInfoField.setPromptText("Informazioni App");

        // Campo per il Load Balancer (opzionale)
        TextField lbField = new TextField();
        lbField.setPromptText("Load Balancer (opzionale)");

        // Pulsanti "Salva" e "Annulla"
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        JFXButton saveButton = new JFXButton("Salva");
        JFXButton cancelButton = new JFXButton("Annulla");
        buttonsBox.getChildren().addAll(saveButton, cancelButton);

        dialogVBox.getChildren().addAll(
                new Label("Aggiungi una nuova App"),
                appNameField,
                appInfoField,
                lbField,
                buttonsBox
        );

        Scene dialogScene = new Scene(dialogVBox, 400, 250);
        dialog.setScene(dialogScene);
        dialog.show();

        // Gestione del pulsante "Salva"
        saveButton.setOnAction(e -> {
            String appName = appNameField.getText().trim();
            String appInfo = appInfoField.getText().trim();
            String lb = lbField.getText().trim();

            if (appName.isEmpty()) {
                showError("Il nome dell'App non può essere vuoto.");
                return;
            }

            // Creazione della nuova App
            App newApp = new App(appName, appInfo, HostType.IP, lb.isEmpty() ? null : lb, new ArrayList<>(), true);
            presenter.addApp(newApp);
            dialog.close();
            showInfo("App aggiunta con successo!");
        });

        // Gestione del pulsante "Annulla"
        cancelButton.setOnAction(e -> {
            dialog.close();
        });
    }

    /**
     * Apre una finestra modale per aggiungere un Load Balancer (LB) a un'App.
     *
     * @param app L'App a cui aggiungere il LB.
     */
    public void openAddLbModal(App app) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Aggiungi Load Balancer");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));
        dialogVBox.setAlignment(Pos.CENTER);

        // Campo per inserire l'IP del LB
        TextField lbIpField = new TextField();
        lbIpField.setPromptText("Indirizzo IP del Load Balancer");

        // Pulsanti "Salva" e "Annulla"
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        JFXButton saveButton = new JFXButton("Salva");
        JFXButton cancelButton = new JFXButton("Annulla");
        buttonsBox.getChildren().addAll(saveButton, cancelButton);

        dialogVBox.getChildren().addAll(
                new Label("Inserisci l'indirizzo IP del Load Balancer"),
                lbIpField,
                buttonsBox
        );

        Scene dialogScene = new Scene(dialogVBox, 400, 150);
        dialog.setScene(dialogScene);
        dialog.show();

        // Gestione del pulsante "Salva"
        saveButton.setOnAction(e -> {
            String lbIp = lbIpField.getText().trim();
            app.setLb(lbIp);
            presenter.updateApp(app); // Assicurati di avere un metodo updateApp nel presenter
            dialog.close();
            showInfo("Load Balancer aggiunto con successo!");
        });

        // Gestione del pulsante "Annulla"
        cancelButton.setOnAction(e -> {
            dialog.close();
        });
    }

    /**
     * Apre una finestra modale per confermare l'eliminazione di un'App.
     *
     * @param app L'App da eliminare.
     */
    public void openDeleteAppModal(App app) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Eliminazione App");
        alert.setHeaderText(null);
        alert.setContentText("Sei sicuro di voler eliminare il gruppo di Host \"" + app.getName() + "\"?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                presenter.removeApp(app);
                showInfo("App eliminata con successo!");
            }
        });
    }

    /**
     * Apre una finestra modale per aggiornare l'IP tramite un ping al LB.
     *
     * @param host L'host da aggiornare.
     * @param lbIp L'indirizzo IP del Load Balancer.
     */
    public void openUpdateIpModal(Host host, String lbIp) {
        // Crea una nuova finestra modale
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Aggiorna IP");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));
        dialogVBox.setAlignment(Pos.CENTER);

        Label label = new Label("Sto cercando di contattare ");
        Label labelLB = new Label(lbIp);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        JFXButton saveButton = new JFXButton("Salva");
        JFXButton cancelButton = new JFXButton("Annulla");
        saveButton.setDisable(true); // Disabilita finché non si ha un IP valido
        buttonsBox.getChildren().addAll(saveButton, cancelButton);

        dialogVBox.getChildren().addAll(
                label,
                labelLB,
                progressIndicator,
                buttonsBox
        );

        Scene dialogScene = new Scene(dialogVBox, 500, 200);
        dialog.setScene(dialogScene);
        dialog.show();

        // Esegui il ping in un thread separato per evitare di bloccare l'interfaccia utente
        new Thread(() -> {
            try {
                // Rileva il sistema operativo per determinare il parametro corretto
                String os = System.getProperty("os.name").toLowerCase();
                String pingParam = os.contains("win") ? "-n" : "-c";

                // Costruisci il comando ping
                ProcessBuilder pb = new ProcessBuilder("ping", pingParam, "1", lbIp);
                pb.redirectErrorStream(true); // Unire stdout e stderr
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                String respondingIp = null;
                boolean pingSuccess = false;

                // Regex per estrarre l'IP
                String ipRegex = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
                Pattern pattern = Pattern.compile(ipRegex);

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        respondingIp = matcher.group();
                        pingSuccess = true;
                        break; // Interrompi il loop dopo aver trovato il primo IP valido
                    }
                }

                int exitCode = process.waitFor();

                if (pingSuccess && respondingIp != null && !respondingIp.isEmpty()) {
                    // Aggiorna il campo di testo con l'IP ottenuto
                    final String finalRespondingIp = respondingIp;
                    javafx.application.Platform.runLater(() -> {
                        label.setText(lbIp);
                        labelLB.setText(finalRespondingIp);
                        labelLB.setStyle("-fx-font-size: 16px;");
                        progressIndicator.setVisible(false);
                        saveButton.setDisable(false);
                    });

                    // Imposta l'azione del pulsante "Salva"
                    javafx.application.Platform.runLater(() -> {
                        saveButton.setOnAction(e -> {
                            host.setIp(finalRespondingIp);
                            presenter.updateHost(host, getAppForHost(host)); // Usa il presenter per aggiornare
                            showInfo("IP aggiornato con successo a: " + finalRespondingIp);
                            dialog.close();
                        });
                    });
                } else {
                    // Ping fallito o IP non trovato
                    javafx.application.Platform.runLater(() -> {
                        label.setText("Ping fallito verso " + lbIp.toUpperCase());
                        progressIndicator.setVisible(false);
                        showError("Impossibile ottenere l'IP dal Load Balancer.");
                        dialog.close();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    label.setText("Errore durante il ping.");
                    progressIndicator.setVisible(false);
                    showError("Errore durante il ping: " + e.getMessage());
                    dialog.close();
                });
            }
        }).start();

        // Gestione del pulsante "Annulla"
        cancelButton.setOnAction(e -> {
            dialog.close();
        });
    }

    /**
     * Ottiene l'App a cui appartiene un determinato Host.
     *
     * @param host L'host di cui trovare l'App.
     * @return L'App a cui appartiene l'host, o null se non trovata.
     */
    private App getAppForHost(Host host) {
        List<App> apps = presenter.getApps();
        for (App app : apps) {
            if (app.getHosts().contains(host)) {
                return app;
            }
        }
        return null;
    }

    /**
     * Apre una finestra modale per confermare la rimozione del Load Balancer da un'App.
     *
     * @param app L'App da cui rimuovere il Load Balancer.
     */
    public void openRemoveLbModal(App app) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Rimozione Load Balancer");
        alert.setHeaderText(null);
        alert.setContentText("Sei sicuro di voler rimuovere il Load Balancer dall'app \"" + app.getName() + "\"?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                app.setLb(null);
                presenter.updateApp(app);
                showInfo("Load Balancer rimosso con successo.");
            }
        });
    }

    /**
     * Valida un indirizzo IP.
     *
     * @param ip L'indirizzo IP da validare.
     * @return True se valido, altrimenti false.
     */
    private boolean isValidIP(String ip) {
        String regex = "^((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.)){3}(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)$";
        return ip.matches(regex);
    }

    /**
     * Valida un FQDN.
     *
     * @param fqdn Il FQDN da validare.
     * @return True se valido, altrimenti false.
     */
    private boolean isValidFQDN(String fqdn) {
        String regex = "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-)$";
        return fqdn.matches(regex);
    }

    /**
     * Mostra un messaggio di errore.
     *
     * @param message Il messaggio di errore.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra un messaggio di informazione.
     *
     * @param message Il messaggio di informazione.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}