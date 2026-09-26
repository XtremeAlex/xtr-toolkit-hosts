package com.xtremealex.toolkit.hosts.mvp.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.mvp.views.MainPresenter;
import com.xtremealex.toolkit.hosts.mvp.views.MainView;
import com.xtremealex.toolkit.hosts.mvp.views.impl.MainPresenterImpl;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable, MainView {

    @FXML private StackPane rootPane;
    @FXML private Pane splashScreen;
    @FXML private AnchorPane mainContent;
    @FXML private VBox appsContainer;
    @FXML private JFXToggleButton musicToggleButton;
    @FXML private JFXButton modifyButton; // Bottone "Modifica"
    @FXML private JFXButton cancelButton; // Bottone "Annulla"
    @FXML private JFXButton saveButton; // Bottone "Salva"
    @FXML private JFXButton addAppButton; // Bottone "Aggiungi App"

    private MainPresenter presenter;
    private MediaPlayer mediaPlayer;
    private final double targetVolume = 0.1;  // Volume massimo desiderato

    // All'interno della classe MainController
    private ModalController modalController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        presenter = new MainPresenterImpl(this);
        presenter.initialize();

        modalController = new ModalController((MainPresenterImpl) presenter); // Inizializza il ModalController

        setupInitialVisibility();
        startSplashScreenTransition();
    }

    /**
     * Imposta la visibilità iniziale degli elementi.
     */
    private void setupInitialVisibility() {
        addAppButton.setVisible(false);
        mainContent.setVisible(false);
        cancelButton.setVisible(false);
        saveButton.setVisible(false);
        splashScreen.setVisible(true);
    }

    /**
     * Inizia la transizione dello splash screen.
     */
    private void startSplashScreenTransition() {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> showMainContentWithAnimation());
        delay.play();
    }

    /**
     * Mostra il contenuto principale con animazione.
     */
    private void showMainContentWithAnimation() {
        mainContent.setVisible(true);
        mainContent.setOpacity(0);

        TranslateTransition slideUp = new TranslateTransition(
                Duration.seconds(1), splashScreen);
        slideUp.setFromY(0);
        slideUp.setToY(-rootPane.getHeight());
        slideUp.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeIn = new FadeTransition(
                Duration.seconds(1), mainContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition transition = new ParallelTransition(slideUp, fadeIn);

        slideUp.setOnFinished(event -> {
            rootPane.getChildren().remove(splashScreen);
            splashScreen.setVisible(false);
            initializeBackgroundMusic();
        });

        transition.play();
    }

    /**
     * Inizializza la musica di background.
     */
    private void initializeBackgroundMusic() {
        URL musicURL = getClass().getResource("/music/background.wav");
        if (musicURL != null) {
            Media media = new Media(musicURL.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);  // Ripeti all'infinito
            mediaPlayer.setVolume(targetVolume);
            mediaPlayer.play();

            // Sincronizza lo stato del toggle con il MediaPlayer
            musicToggleButton.setSelected(true);

            // Listener per sincronizzare lo stato del toggle con il MediaPlayer
            mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
                if (newStatus == MediaPlayer.Status.STOPPED || newStatus == MediaPlayer.Status.PAUSED) {
                    if (musicToggleButton.isSelected()) {
                        musicToggleButton.setSelected(false);
                    }
                } else if (newStatus == MediaPlayer.Status.PLAYING) {
                    if (!musicToggleButton.isSelected()) {
                        musicToggleButton.setSelected(true);
                    }
                }
            });

            bindMusicToggleButton();
        } else {
            showError("Errore: file audio non trovato!");
        }
    }

    /**
     * Collega il pulsante toggle della musica con il MediaPlayer.
     */
    private void bindMusicToggleButton() {
        musicToggleButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (mediaPlayer != null) {
                handleMusicToggle(isNowSelected);
            }
        });
    }

    /**
     * Gestisce il toggle della musica.
     *
     * @param isSelected Se la musica deve essere attivata.
     */
    private void handleMusicToggle(boolean isSelected) {
        if (isSelected) {
            // Avvia la musica con fade-in
            fadeInMusic();
        } else {
            // Ferma la musica con fade-out
            fadeOutMusic();
        }
    }

    /**
     * Esegue un fade-in della musica.
     */
    private void fadeInMusic() {
        if (mediaPlayer == null) return;

        // Disabilita il toggle durante la transizione
        musicToggleButton.setDisable(true);

        mediaPlayer.setVolume(0.0);
        mediaPlayer.play();

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(mediaPlayer.volumeProperty(), 0.0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(mediaPlayer.volumeProperty(), targetVolume))
        );

        fadeIn.setOnFinished(event -> {
            musicToggleButton.setDisable(false);
        });

        fadeIn.play();
    }

    /**
     * Esegue un fade-out della musica.
     */
    private void fadeOutMusic() {
        if (mediaPlayer == null) return;

        // Disabilita il toggle durante la transizione
        musicToggleButton.setDisable(true);

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(mediaPlayer.volumeProperty(), targetVolume)),
                new KeyFrame(Duration.seconds(2), new KeyValue(mediaPlayer.volumeProperty(), 0.0))
        );

        fadeOut.setOnFinished(event -> {
            mediaPlayer.pause();
            musicToggleButton.setDisable(false);
        });

        fadeOut.play();
    }

    @Override
    public void setApps(List<App> apps) {
        renderApps(apps);
    }

    @Override
    public void refreshApps() {
        List<App> apps = presenter.getApps();
        renderApps(apps);
    }

    @Override
    public void setEditing(boolean isEditing) {
        if (isEditing) {
            setupEditModeButtons();
            modifyButton.setVisible(false);  // Nasconde il bottone "Modifica"
            cancelButton.setVisible(true);   // Mostra "Annulla"
            saveButton.setVisible(true);     // Mostra "Salva"
            addAppButton.setVisible(true);   // Mostra "Aggiungi App" solo in modalità modifica
        } else {
            setupViewModeButtons();
            modifyButton.setVisible(true);   // Mostra il bottone "Modifica"
            cancelButton.setVisible(false);  // Nascondi "Annulla"
            saveButton.setVisible(false);    // Nascondi "Salva"
            addAppButton.setVisible(false);  // Nascondi "Aggiungi App"
        }

        refreshApps();
    }

    @Override
    public void toggleEditMode(boolean isEditing) {
        presenter.toggleEditMode(isEditing);
    }

    @Override
    public void showMainContent() {
        mainContent.setVisible(true);
    }

    /**
     * Configura i bottoni per la modalità di modifica.
     * In questo caso, i pulsanti sono già presenti nello FXML e vengono solo resi visibili.
     */
    private void setupEditModeButtons() {
        // Nessuna azione aggiuntiva necessaria poiché "Annulla" e "Salva" sono già visibili
    }

    /**
     * Configura i bottoni per la modalità di visualizzazione.
     * In questo caso, i pulsanti "Annulla" e "Salva" vengono nascosti.
     */
    private void setupViewModeButtons() {
        // Nessuna azione aggiuntiva necessaria poiché i bottoni vengono nascosti nel setEditing
    }

    /**
     * Rende le app nella GUI.
     *
     * @param apps La lista di app da rendere.
     */
    private void renderApps(List<App> apps) {
        appsContainer.getChildren().clear();

        boolean isEditing = presenter.isEditing();

        for (App app : apps) {
            VBox appContainer = createAppContainer(app, isEditing);
            appsContainer.getChildren().add(appContainer);
        }
    }

    /**
     * Crea il container per una singola app.
     *
     * @param app       L'app da visualizzare.
     * @param isEditing Se è in modalità di modifica.
     * @return Il VBox contenente l'app.
     */
    private VBox createAppContainer(App app, boolean isEditing) {
        VBox appContainer = new VBox(5);
        appContainer.setPadding(new Insets(10));
        appContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        appContainer.setPrefWidth(600);

        HBox appHeader = createAppHeader(app, isEditing); // Chiamata diretta al metodo privato
        appContainer.getChildren().add(appHeader);

        // Visualizza le informazioni del LB se presenti e non in modalità modifica
        if (!isEditing && app.getLb() != null && !app.getLb().isEmpty()) {
            Label lbLabel = new Label(app.getLb());
            lbLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
            appContainer.getChildren().add(lbLabel);
        }

        if (!isEditing) {
            for (Host host : app.getHosts()) {
                HBox hostBox = createHostBox(host);
                appContainer.getChildren().add(hostBox);
            }
        } else {
            for (Host host : app.getHosts()) {
                HBox hostBox = createEditableHostBox(host, app);
                appContainer.getChildren().add(hostBox);
            }

            // Pulsante "Aggiungi Host" (singolo pulsante)
            JFXButton addButton = new JFXButton("Aggiungi Host");
            addButton.setPrefWidth(150);
            addButton.setOnAction(e -> {
                Host newHost = new Host("Nuovo IP", "Nuovo FQDN", true);
                presenter.addHost(newHost, app);
            });
            appContainer.getChildren().add(addButton);
        }

        return appContainer;
    }

    /**
     * Crea l'intestazione per una singola app.
     *
     * @param app       L'app da visualizzare.
     * @param isEditing Se è in modalità di modifica.
     * @return L'HBox contenente l'intestazione.
     */
    private HBox createAppHeader(App app, boolean isEditing) {
        HBox appHeader = new HBox(10);
        appHeader.setAlignment(Pos.CENTER_LEFT);
        appHeader.setPadding(new Insets(5, 0, 5, 0));

        // Debugging: Stampa i valori attuali di name e lb
        System.out.println("Creando header per App: " + app.getName() + ", LB: " + app.getLb());

        // --- Nome App ---
        if (isEditing) {
            TextField appNameField = new TextField(app.getName() != null ? app.getName() : "Unnamed App");
            appNameField.setPrefWidth(150);
            appNameField.textProperty().addListener((obs, oldText, newText) -> {
                app.setName(newText.trim().isEmpty() ? "Unnamed App" : newText.trim());
            });

            appHeader.getChildren().addAll(appNameField);
        } else {
            Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
            appLabel.setStyle("-fx-font-size: 16px;");
            appHeader.getChildren().add(appLabel);
        }

        // --- Load Balancer ---
        if (isEditing) {
            if (app.getLb() != null && !app.getLb().isEmpty()) {
                TextField lbField = new TextField(app.getLb());
                lbField.setPrefWidth(200);
                lbField.setPromptText("Load Balancer");
                lbField.textProperty().addListener((obs, oldText, newText) -> {
                    app.setLb(newText.trim().isEmpty() ? null : newText.trim());
                });

                // Pulsante "Elimina Load Balancer"
                // Pulsante "Elimina"
                JFXButton deleteLbButton = new JFXButton("❌");
                deleteLbButton.getStyleClass().add("delete-button");
                deleteLbButton.setOnAction(e -> {
                    modalController.openRemoveLbModal(app);
                });

                appHeader.getChildren().addAll(lbField, deleteLbButton);
            } else {
                // LB non esiste: mostra pulsante per aggiungerlo
                JFXButton addLbButton = new JFXButton("Aggiungi Load Balancer");
                addLbButton.setPrefWidth(245);
                addLbButton.setOnAction(e -> {
                    modalController.openAddLbModal(app);
                });
                appHeader.getChildren().add(addLbButton);
            }

            // --- Pulsante "Elimina App" ---
            if (app.getName() != null) {
                JFXButton deleteAppButton = new JFXButton("Elimina App");
                deleteAppButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                deleteAppButton.setOnAction(e -> {
                    modalController.openDeleteAppModal(app);
                });
                appHeader.getChildren().add(deleteAppButton);
            }
        } else {
            // In modalità visualizzazione, mostra il LB se presente
            if (app.getLb() != null && !app.getLb().isEmpty()) {
                //Label lbLabel = new Label("LB: " + app.getLb());
                //lbLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
                //appHeader.getChildren().add(lbLabel);
            }
        }

        return appHeader;
    }

    /**
     * Crea un HBox per visualizzare un host in modalità di visualizzazione.
     *
     * @param host L'host da visualizzare.
     * @return L'HBox contenente l'host.
     */
    private HBox createHostBox(Host host) {
        HBox hostBox = new HBox(10);
        hostBox.setAlignment(Pos.CENTER_LEFT);

        JFXToggleButton toggleButton = new JFXToggleButton();
        toggleButton.setSelected(host.isEnabled());
        toggleButton.setText(host.isEnabled() ? "ON" : "OFF");
        toggleButton.getStyleClass().add("custom-jfx-toggle-button");

        Label ipLabel = new Label(host.getIp());
        Label fqdnLabel = new Label(host.getFqdn());
        ipLabel.setFont(new Font("", 14));
        fqdnLabel.setFont(new Font("", 14));

        toggleButton.setOnAction(e -> {
            boolean newStatus = toggleButton.isSelected();
            host.setEnabled(newStatus);
            toggleButton.setText(newStatus ? "ON" : "OFF");
            System.out.println((newStatus ? "Attivato " : "Disattivato ") + "IP: " + host.getIp() + " FQDN: " + host.getFqdn());
        });

        hostBox.getChildren().addAll(toggleButton, ipLabel, fqdnLabel);
        return hostBox;
    }

    /**
     * Crea un HBox per visualizzare e modificare un host in modalità di modifica.
     *
     * @param host L'host da visualizzare e modificare.
     * @param app  L'app a cui appartiene l'host.
     * @return L'HBox contenente l'host modificabile.
     */
    private HBox createEditableHostBox(Host host, App app) {
        HBox hostBox = new HBox(10);
        hostBox.setAlignment(Pos.CENTER_LEFT);

        // Campo di testo per IP
        TextField ipField = new TextField(host.getIp());
        ipField.setPrefWidth(150);

        // Campo di testo per FQDN
        TextField fqdnField = new TextField(host.getFqdn());
        fqdnField.setPrefWidth(200);

        // Pulsante "Elimina"
        JFXButton deleteButton = new JFXButton("❌");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> {
            presenter.removeHost(host);
        });

        // Listener per aggiornare l'host quando i campi di testo vengono modificati
        ipField.textProperty().addListener((obs, oldText, newText) -> {
            host.setIp(newText);
        });

        fqdnField.textProperty().addListener((obs, oldText, newText) -> {
            host.setFqdn(newText);
        });

        hostBox.getChildren().addAll(ipField, fqdnField, deleteButton);

        // Se l'app ha un LB, aggiungi il pulsante "Aggiorna IP"
        if (app.getLb() != null && !app.getLb().isEmpty()) {
            JFXButton updateIpButton = new JFXButton("Aggiorna IP");
            updateIpButton.setOnAction(e -> {
                modalController.openUpdateIpModal(host, app.getLb());
            });
            hostBox.getChildren().add(updateIpButton);
        }

        return hostBox;
    }

    /**
     * Mostra un messaggio di errore all'utente.
     *
     * @param message Il messaggio da mostrare.
     */
    @Override
    public void showError(String message) {
        // Utilizza un dialogo di JavaFX per mostrare gli errori
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra un messaggio di informazione all'utente.
     *
     * @param message Il messaggio da mostrare.
     */
    @Override
    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gestisce l'azione del bottone "Aggiungi App".
     *
     * @param event L'evento di azione.
     */
    @FXML
    private void handleAddAppAction(ActionEvent event) {
        modalController.openAddAppModal(); // Usa il metodo di ModalController
    }

    /**
     * Gestisce l'azione del bottone "Salva".
     *
     * @param event L'evento di azione.
     */
    @FXML
    private void handleSaveEdit(ActionEvent event) {
        presenter.saveChanges();
        toggleEditMode(false);
    }

    /**
     * Gestisce l'azione del bottone "Annulla".
     *
     * @param event L'evento di azione.
     */
    @FXML
    private void handleCancelEdit(ActionEvent event) {
        presenter.cancelChanges();
        toggleEditMode(false);
    }

    @FXML
    public void handleModifyAction(ActionEvent actionEvent) {
        presenter.handleModifyAction();
    }

}