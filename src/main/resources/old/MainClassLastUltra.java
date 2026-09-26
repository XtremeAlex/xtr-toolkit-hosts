package old;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MainClassLastUltra extends Application {

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

        // Load Overpass font
        //Font font = Font.loadFont(new FileInputStream("src/main/resources/fonts/Overpass-Light.ttf"), 20);


       Font font = Font.font("Aral", FontWeight.NORMAL, 16);  // Aumentato il font

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

        //FONT
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    // Funzione per creare una copia profonda delle app (per il ripristino in caso di annullamento)
    private List<App> deepCopyApps(List<App> apps) {
        // Crea una copia profonda delle App e dei loro Host
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
            VBox appContainer = new VBox(15);
            HBox appHeader = new HBox(15);
            appHeader.setAlignment(Pos.CENTER_LEFT);

            // Pulsante Aggiorna se bilanciatore
            if (app.getHostType() == HostType.BALANCER) {
                Button refreshButton = new Button("Aggiorna");
                refreshButton.setMinWidth(120); // Imposta larghezza fissa per uniformità
                refreshButton.setStyle("-fx-background-color: lightblue;");
                refreshButton.setOnAction(e -> System.out.println("Aggiornamento IP per " + app.getLb()));
                appHeader.getChildren().add(refreshButton);
            }

            // Label App
            Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
            appLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            appLabel.setMinWidth(150); // Imposta larghezza fissa per uniformità
            appLabel.setStyle("-fx-padding: 10; -fx-background-color: lightgray; -fx-border-color: gray; -fx-border-width: 1;");
            appHeader.getChildren().add(appLabel);

            // Pulsante Aggiungi per IP/FQDN
            if (isEditing) {
                Button addButton = new Button("Aggiungi");
                addButton.setMinWidth(120); // Imposta larghezza fissa per uniformità
                addButton.setOnAction(e -> System.out.println("Aggiungi nuovo record IP/FQDN."));
                appHeader.getChildren().add(addButton);
            }

            appContainer.getChildren().add(appHeader);

            // Modalità Normale (raggruppa gli IP)
            if (!isEditing) {
                // Raggruppa per IP
                app.getHosts().forEach(host -> {
                    HBox hostBox = new HBox(10);
                    hostBox.setAlignment(Pos.CENTER_LEFT);

                    ToggleButton toggleButton = new ToggleButton(host.isEnabled() ? "ON" : "OFF");
                    toggleButton.setSelected(host.isEnabled());
                    toggleButton.setStyle(host.isEnabled() ? "-fx-background-color: lightgreen;" : "-fx-background-color: lightcoral;");
                    toggleButton.setOnAction(e -> {
                        boolean newStatus = toggleButton.isSelected();
                        host.setEnabled(newStatus);
                        toggleButton.setText(newStatus ? "ON" : "OFF");

                        // Effetto di transizione di colore
                        String fromColor = newStatus ? "lightcoral" : "lightgreen";
                        String toColor = newStatus ? "lightgreen" : "lightcoral";
                        animateButtonColorChange(toggleButton, fromColor, toColor);

                        toggleButton.setStyle(newStatus ? "-fx-background-color: lightgreen;" : "-fx-background-color: lightcoral;");

                        // Log della modifica
                        System.out.println((newStatus ? "Attivato " : "Disattivato ") + "IP: " + host.getIp() + " FQDN: " + host.getFqdn());
                    });

                    Label ipLabel = new Label(host.getIp());
                    Label fqdnLabel = new Label(host.getFqdn());
                    ipLabel.setFont(font);
                    fqdnLabel.setFont(font);
                    hostBox.getChildren().addAll(toggleButton, ipLabel, fqdnLabel);
                    appContainer.getChildren().add(hostBox);
                });
            } else {
                // Modalità Modifica (mostra tutto separatamente)
                for (Host host : app.getHosts()) {
                    HBox hostBox = new HBox(10);
                    hostBox.setAlignment(Pos.CENTER_LEFT);

                    TextField ipField = new TextField(host.getIp());
                    TextField fqdnField = new TextField(host.getFqdn());
                    Button deleteButton = new Button("X");
                    deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    deleteButton.setOnAction(e -> {
                        // Animazione di "distruzione" al clic
                        ScaleTransition st = new ScaleTransition(Duration.millis(300), hostBox);
                        st.setFromX(1.0);
                        st.setToX(0.0);
                        st.setFromY(1.0);
                        st.setToY(0.0);
                        st.setOnFinished(event -> {
                            System.out.println("Eliminato FQDN: " + host.getFqdn());
                            app.getHosts().remove(host);
                            toggleEditMode(true, mainLayout, apps, font, editButtonsBox);  // Ricarica la vista di modifica
                        });
                        st.play();
                    });

                    hostBox.getChildren().addAll(ipField, fqdnField, deleteButton);
                    appContainer.getChildren().add(hostBox);
                }
            }

            mainLayout.getChildren().add(appContainer);
        }
    }

    // Funzione per animare il cambiamento di colore del pulsante ON/OFF
    private void animateButtonColorChange(ToggleButton button, String fromColor, String toColor) {
        Timeline timeline = new Timeline();
        KeyValue kv1 = new KeyValue(button.styleProperty(), "-fx-background-color: " + fromColor);
        KeyValue kv2 = new KeyValue(button.styleProperty(), "-fx-background-color: " + toColor);
        KeyFrame kf1 = new KeyFrame(Duration.ZERO, kv1);
        KeyFrame kf2 = new KeyFrame(Duration.millis(300), kv2);
        timeline.getKeyFrames().addAll(kf1, kf2);
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
