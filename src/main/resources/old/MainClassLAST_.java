package old;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainClassLAST_ extends Application {

    private boolean isEditing = false;
    private List<App> originalApps;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Simula il caricamento dei dati (App con Hosts e FQDN)
        List<App> apps = HostParser.parseHostsFile("/etc/hosts");

        // Copia dei dati per ripristinare in caso di annullamento
        originalApps = deepCopyApps(apps);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        Font font = Font.font("Arial", FontWeight.NORMAL, 14);

        // Pulsanti Modifica/Annulla/Salva
        HBox editButtonsBox = new HBox(10);
        editButtonsBox.setAlignment(Pos.CENTER);
        Button modifyButton = new Button("Modifica");
        Button cancelButton = new Button("Annulla");
        Button saveButton = new Button("Salva");

        modifyButton.setOnAction(e -> toggleEditMode(true, mainLayout, apps, font, editButtonsBox));
        cancelButton.setOnAction(e -> {
            System.out.println("Modifiche annullate.");
            toggleEditMode(false, mainLayout, originalApps, font, editButtonsBox); // Ripristina i dati originali
        });
        saveButton.setOnAction(e -> {
            System.out.println("Modifiche salvate.");
            originalApps = deepCopyApps(apps);  // Aggiorna i dati originali con quelli modificati
            toggleEditMode(false, mainLayout, apps, font, editButtonsBox); // Torna alla vista iniziale
        });

        editButtonsBox.getChildren().add(modifyButton);
        mainLayout.getChildren().add(editButtonsBox);

        renderApps(apps, mainLayout, font, editButtonsBox);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 600, 400);
        primaryStage.setTitle("Gestione Hosts");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Funzione per creare una copia profonda delle app (per il ripristino in caso di annullamento)
    private List<App> deepCopyApps(List<App> apps) {
        List<App> copiedApps = new ArrayList<>();
        for (App app : apps) {
            App newApp = new App();
            newApp.setName(app.getName());
            newApp.setInfo(app.getInfo());
            newApp.setHostType(app.getHostType());
            newApp.setLb(app.getLb());
            newApp.setAutoload(app.isAutoload());

            List<Host> copiedHosts = new ArrayList<>();
            for (Host host : app.getHosts()) {
                copiedHosts.add(new Host(host.getIp(), host.getFqdn(), host.isEnabled()));
            }
            newApp.setHosts(copiedHosts);
            copiedApps.add(newApp);
        }
        return copiedApps;
    }

    private void toggleEditMode(boolean editMode, VBox mainLayout, List<App> apps, Font font, HBox editButtonsBox) {
        isEditing = editMode;
        mainLayout.getChildren().clear();

        if (editMode) {
            editButtonsBox.getChildren().clear();
            Button cancelButton = new Button("Annulla");
            Button saveButton = new Button("Salva");

            cancelButton.setOnAction(e -> {
                System.out.println("Modifiche annullate.");
                toggleEditMode(false, mainLayout, originalApps, font, editButtonsBox); // Ripristina i dati originali
            });
            saveButton.setOnAction(e -> {
                System.out.println("Modifiche salvate.");
                originalApps = deepCopyApps(apps);  // Aggiorna i dati originali con quelli modificati
                toggleEditMode(false, mainLayout, apps, font, editButtonsBox); // Torna alla vista iniziale
            });

            editButtonsBox.getChildren().addAll(cancelButton, saveButton);
        } else {
            editButtonsBox.getChildren().clear();
            Button modifyButton = new Button("Modifica");
            modifyButton.setOnAction(e -> toggleEditMode(true, mainLayout, apps, font, editButtonsBox));
            editButtonsBox.getChildren().add(modifyButton);
        }

        mainLayout.getChildren().add(editButtonsBox);
        renderApps(apps, mainLayout, font, editButtonsBox);
    }

    private void renderApps(List<App> apps, VBox mainLayout, Font font, HBox editButtonsBox) {
        for (App app : apps) {
            VBox appContainer = new VBox(10);
            HBox appHeader = new HBox(10);
            appHeader.setAlignment(Pos.CENTER_LEFT);

            // Pulsante Aggiorna se bilanciatore
            if (app.getHostType() == HostType.BALANCER) {
                Button refreshButton = new Button("Aggiorna");
                refreshButton.setStyle("-fx-background-color: lightblue;");
                refreshButton.setOnAction(e -> System.out.println("Aggiornamento IP per " + app.getLb()));
                appHeader.getChildren().add(refreshButton);
            }

            // Label App
            Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
            appLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            appLabel.setStyle("-fx-padding: 10; -fx-background-color: lightgray; -fx-border-color: gray; -fx-border-width: 1;");
            appHeader.getChildren().add(appLabel);

            // Pulsante Aggiungi per IP/FQDN
            if (isEditing) {
                Button addButton = new Button("Aggiungi");
                addButton.setOnAction(e -> System.out.println("Aggiungi nuovo record IP/FQDN."));
                appHeader.getChildren().add(addButton);
            }

            appContainer.getChildren().add(appHeader);

            // Raggruppamento per IP quando non siamo in modalità modifica
            if (!isEditing) {
                Map<String, List<Host>> ipToHostMap = new HashMap<>();
                for (Host host : app.getHosts()) {
                    String ip = host.getIp();
                    ipToHostMap.putIfAbsent(ip, new ArrayList<>());
                    ipToHostMap.get(ip).add(host);
                }

                // Aggiungiamo gli IP raggruppati con i loro FQDN
                for (String ip : ipToHostMap.keySet()) {
                    List<Host> hosts = ipToHostMap.get(ip);

                    HBox hostBox = new HBox(10);
                    hostBox.setAlignment(Pos.CENTER_LEFT);

                    // Etichetta per IP
                    Label ipLabel = new Label(ip);
                    ipLabel.setFont(font);

                    VBox fqdnBox = new VBox(5);
                    fqdnBox.setPadding(new Insets(5, 0, 0, 10));

                    // FQDN abilitati prima
                    for (Host host : hosts) {
                        if (host.isEnabled()) {
                            Label fqdnLabel = new Label(host.getFqdn());
                            fqdnLabel.setFont(font);
                            fqdnBox.getChildren().add(fqdnLabel);
                        }
                    }

                    // FQDN disabilitati dopo
                    for (Host host : hosts) {
                        if (!host.isEnabled()) {
                            Label fqdnLabel = new Label(host.getFqdn());
                            fqdnLabel.setFont(font);
                            fqdnBox.getChildren().add(fqdnLabel);
                        }
                    }

                    // Toggle ON/OFF
                    boolean hasEnabledHosts = hosts.stream().anyMatch(Host::isEnabled);
                    ToggleButton toggleButton = new ToggleButton(hasEnabledHosts ? "ON" : "OFF");
                    toggleButton.setSelected(hasEnabledHosts);
                    toggleButton.setStyle(hasEnabledHosts ? "-fx-background-color: lightgreen;" : "-fx-background-color: lightcoral;");
                    toggleButton.setOnAction(e -> {
                        boolean newStatus = toggleButton.isSelected();
                        toggleButton.setText(newStatus ? "ON" : "OFF");
                        toggleButton.setStyle(newStatus ? "-fx-background-color: lightgreen;" : "-fx-background-color: lightcoral;");
                        hosts.forEach(host -> host.setEnabled(newStatus));
                    });

                    hostBox.getChildren().addAll(toggleButton, ipLabel, fqdnBox);
                    appContainer.getChildren().add(hostBox);
                }
            } else {
                // Modalità Modifica
                for (Host host : app.getHosts()) {
                    HBox hostBox = new HBox(10);
                    hostBox.setAlignment(Pos.CENTER_LEFT);

                    TextField ipField = new TextField(host.getIp());
                    TextField fqdnField = new TextField(host.getFqdn());
                    Button deleteButton = new Button("X");
                    deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    deleteButton.setOnAction(e -> {
                        System.out.println("Eliminato FQDN: " + host.getFqdn());
                        app.getHosts().remove(host);

                        toggleEditMode(true, mainLayout, apps, font, editButtonsBox);  // Ricarica la vista di modifica
                    });

                    hostBox.getChildren().addAll(ipField, fqdnField, deleteButton);
                    appContainer.getChildren().add(hostBox);
                }
            }

            mainLayout.getChildren().add(appContainer);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
