package old;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController_LAST implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Pane splashScreen;
    @FXML private AnchorPane mainContent;
    @FXML private ToggleButton musicToggleButton;
    @FXML private ToggleButton musicToggleButtonMain;
    @FXML private VBox mainLayout;
    @FXML private HBox editButtonsBox;
    @FXML private Button modifyButton;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox appsContainer;

    @FXML private Label appTitle;
    @FXML private Label appAuthor; // Se desideri animare anche l'autore

    private boolean isEditing = false;
    private List<App> apps;
    private List<App> originalApps;
    private Font font;
    private AudioClip backgroundMusic;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Carica le app (Simula il caricamento dei dati)
            apps = HostParser.parseHostsFile("/etc/hosts");
            originalApps = deepCopyApps(apps);

            font = Font.font("Arial", 14);

            // Inizializza la musica
            URL musicURL = getClass().getResource("/music/background.wav");
            if (musicURL != null) {
                backgroundMusic = new AudioClip(musicURL.toString());
                backgroundMusic.setCycleCount(AudioClip.INDEFINITE);
                backgroundMusic.setVolume(0.3); // Imposta il volume al 30%
                backgroundMusic.play();
            } else {
                System.out.println("Errore: file audio non trovato!");
            }

            // Imposta lo stato iniziale dei ToggleButton
            musicToggleButton.setSelected(true);
            musicToggleButtonMain.setSelected(true);

            // Sincronizza lo stato dei due ToggleButton
            musicToggleButton.selectedProperty().bindBidirectional(musicToggleButtonMain.selectedProperty());

            // La splashScreen deve essere visibile e coprire tutto
            splashScreen.setVisible(true);
            mainContent.setVisible(true); // Visibile fin dall'inizio
            //mainContent.setVisible(false); // Non visibile finché la splashScreen non scompare

            // Porta la splashScreen in primo piano
            splashScreen.toFront();
            // Renderizza le app prima dell'animazione
            renderApps();


            // Avvia il timer per la transizione dopo 3 secondi
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> showMainContent());
            delay.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMainContent() {
        // Rendi visibile il contenuto principale ma con opacità 0
        mainContent.setVisible(true);
        mainContent.setOpacity(0);

        // Anima la splashScreen verso l'alto
        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(1), splashScreen);
        slideUp.setFromY(0);
        slideUp.setToY(-rootPane.getHeight());
        slideUp.setInterpolator(Interpolator.EASE_IN);

        // Anima l'opacità del mainContent da 0 a 1
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), mainContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Esegui le due animazioni in parallelo
        ParallelTransition transition = new ParallelTransition(slideUp, fadeIn);

        slideUp.setOnFinished(event -> {
            rootPane.getChildren().remove(splashScreen); // Rimuove la splashScreen dopo l'animazione
        });

        transition.play();
    }

    private void showMainContent2() {
        // Anima la splashScreen verso l'alto
        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(1), splashScreen);
        slideUp.setFromY(0);
        slideUp.setToY(-rootPane.getHeight());
        slideUp.setInterpolator(Interpolator.EASE_IN);

        slideUp.setOnFinished(event -> {
            rootPane.getChildren().remove(splashScreen); // Rimuove la splashScreen dopo l'animazione
        });

        slideUp.play();
    }



    @FXML
    private void handleMusicToggle() {
        boolean isSelected = musicToggleButton.isSelected(); // Usa il primo ToggleButton come riferimento
        if (isSelected) {
            if (backgroundMusic != null) {
                backgroundMusic.play();
            }
        } else {
            if (backgroundMusic != null) {
                backgroundMusic.stop();
            }
        }
    }

    @FXML
    private void handleModifyAction() {
        toggleEditMode(true);
    }

    private List<App> deepCopyApps(List<App> apps) {
        List<App> copiedApps = new ArrayList<>();
        for (App app : apps) {
            App newApp = new App(app.getName(), app.getInfo(), app.getHostType(), app.getLb(), new ArrayList<>(), app.isAutoload());
            for (Host host : app.getHosts()) {
                newApp.getHosts().add(new Host(host.getIp(), host.getFqdn(), host.isEnabled()));
            }
            copiedApps.add(newApp);
        }
        return copiedApps;
    }

    private void toggleEditMode(boolean editMode) {
        isEditing = editMode;
        appsContainer.getChildren().clear();

        if (editMode) {
            editButtonsBox.getChildren().clear();
            Button cancelButton = new Button("Annulla");
            Button saveButton = new Button("Salva");

            cancelButton.setOnAction(e -> {
                apps = deepCopyApps(originalApps); // Ripristina i dati originali
                toggleEditMode(false);
            });

            saveButton.setOnAction(e -> {
                originalApps = deepCopyApps(apps);  // Aggiorna i dati originali con quelli modificati
                toggleEditMode(false);
            });

            editButtonsBox.getChildren().addAll(cancelButton, saveButton);
        } else {
            editButtonsBox.getChildren().clear();
            modifyButton = new Button("Modifica");
            modifyButton.setOnAction(e -> toggleEditMode(true));
            editButtonsBox.getChildren().add(modifyButton);
        }

        renderApps();
    }

    private void renderApps() {
        appsContainer.getChildren().clear();
        for (App app : apps) {
            VBox appContainer = new VBox(10);
            appContainer.setPadding(new Insets(10));
            HBox appHeader = new HBox(10);
            appHeader.setAlignment(Pos.CENTER_LEFT);

            // Label App
            Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
            appLabel.setFont(Font.font("Arial", 16));
            appHeader.getChildren().add(appLabel);

            // Pulsante Aggiungi per IP/FQDN
            if (isEditing) {
                Button addButton = new Button("+");
                addButton.setOnAction(e -> System.out.println("Aggiungi nuovo record IP/FQDN."));
                appHeader.getChildren().add(addButton);
            }

            appContainer.getChildren().add(appHeader);

            if (!isEditing) {
                app.getHosts().forEach(host -> {
                    HBox hostBox = new HBox(10);
                    hostBox.setAlignment(Pos.CENTER_LEFT);

                    ToggleButton toggleButton = new ToggleButton(host.isEnabled() ? "ON" : "OFF");
                    toggleButton.setSelected(host.isEnabled());

                    // Applica le classi di stile
                    toggleButton.getStyleClass().add("toggle-button");
                    toggleButton.getStyleClass().add(host.isEnabled() ? "toggle-button-on" : "toggle-button-off");

                    toggleButton.setOnAction(e -> {
                        boolean newStatus = toggleButton.isSelected();
                        host.setEnabled(newStatus);
                        toggleButton.setText(newStatus ? "ON" : "OFF");

                        // Aggiorna le classi di stile
                        toggleButton.getStyleClass().removeAll("toggle-button-on", "toggle-button-off");
                        toggleButton.getStyleClass().add(newStatus ? "toggle-button-on" : "toggle-button-off");

                        System.out.println((newStatus ? "Attivato " : "Disattivato ") + "IP: " + host.getIp() + " FQDN: " + host.getFqdn());
                    });

                    Label fqdnLabel = new Label(host.getFqdn());
                    fqdnLabel.setFont(font);
                    hostBox.getChildren().addAll(toggleButton, fqdnLabel);
                    appContainer.getChildren().add(hostBox);
                });
            } else {
                for (Host host : app.getHosts()) {
                    HBox hostBox = new HBox(10);
                    hostBox.setAlignment(Pos.CENTER_LEFT);

                    TextField fqdnField = new TextField(host.getFqdn());
                    Button deleteButton = new Button("X");
                    deleteButton.getStyleClass().add("delete-button"); // Applica la classe di stile per il pulsante 'X' in rosso

                    deleteButton.setOnAction(e -> {
                        app.getHosts().remove(host);
                        toggleEditMode(true);  // Ricarica la vista di modifica
                    });

                    hostBox.getChildren().addAll(fqdnField, deleteButton);
                    appContainer.getChildren().add(hostBox);
                }
            }

            appsContainer.getChildren().add(appContainer);
        }
    }
}
