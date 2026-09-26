package com.xtremealex.toolkit.hosts.mvp.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXToggleButton;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.mvp.views.HostEditCell;
import com.xtremealex.toolkit.hosts.mvp.views.MainPresenter;
import com.xtremealex.toolkit.hosts.mvp.views.MainView;
import com.xtremealex.toolkit.hosts.mvp.views.impl.MainPresenterImpl;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable, MainView {

    @FXML private StackPane rootPane;
    @FXML private Pane splashScreen;
    @FXML private AnchorPane mainContent;
    @FXML private VBox appsContainer;
    @FXML private HBox editButtonsBox;
    @FXML private JFXToggleButton musicToggleButton = new JFXToggleButton();

    @FXML private Pane editPane;
    @FXML private JFXListView<Host> editHostListView;


    private MainPresenter presenter;
    private AudioClip backgroundMusic;
    private MediaPlayer mediaPlayer;
    private final double targetVolume = 0.1;  // Volume massimo desiderato

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        presenter = new MainPresenterImpl(this);
        presenter.initialize();


        //musicToggleButton.set
        setupInitialVisibility();

        startSplashScreenTransition();

        editPane.prefWidthProperty().bind(rootPane.widthProperty());
        editPane.prefHeightProperty().bind(rootPane.heightProperty());

        // Inizializza la musica e sincronizza lo stato iniziale con il pulsante
        initializeBackgroundMusic();

        // Collega il pulsante al comportamento della musica
        bindMusicToggleButton();
    }

    @Override
    public void showMainContent() {
        mainContent.setVisible(true);
    }

    @Override
    public void showError(String message) {
        // Puoi utilizzare un dialogo di MaterialFX se desideri
        System.err.println(message);
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
        // Aggiorna l'interfaccia in base allo stato di modifica
        editButtonsBox.getChildren().clear();

        if (isEditing) {
            setupEditModeButtons();
            showEditPane();
        } else {
            setupViewModeButtons();
            hideEditPane();
        }

        refreshApps();
    }


    private void initializeBackgroundMusic() {
        URL musicURL = getClass().getResource("/music/background.wav");
        if (musicURL != null) {
            Media media = new Media(musicURL.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);  // Ripeti all'infinito

            // Controlla lo stato iniziale del pulsante e imposta il comportamento della musica
            if (musicToggleButton.isSelected()) {
                fadeInMusic();  // Avvia la musica con fade in
            } else {
                mediaPlayer.setVolume(0.0);  // Mantieni il volume a 0 se il pulsante è OFF
                mediaPlayer.stop();  // Ferma la musica inizialmente
            }
        } else {
            System.err.println("Errore: file audio non trovato!");
        }
    }

    private void bindMusicToggleButton() {
        musicToggleButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            handleMusicToggle(isNowSelected);
        });
    }

    private void bindMusicToggleButtons() {
        // Il binding non è più necessario, usiamo solo un toggle button
        musicToggleButton.setSelected(true);  // Imposta il pulsante come selezionato per default (musica attiva)

        // Aggiungi listener per gestire il toggle della musica
        musicToggleButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            handleMusicToggle(isNowSelected);
        });
    }

    private void setupInitialVisibility() {
        splashScreen.setVisible(true);
        mainContent.setVisible(false);
        editPane.setVisible(false); // Assicurati che l'editPane sia nascosto all'inizio
    }

    private void startSplashScreenTransition() {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> showMainContentWithAnimation());
        delay.play();
    }

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
            initializeBackgroundMusic();
        });

        transition.play();
    }




    @FXML
    private void handleMusicToggle(ActionEvent event) {
        // Disabilita il pulsante durante la transizione per evitare clic ripetuti
        musicToggleButton.setDisable(true);

        boolean isSelected = musicToggleButton.isSelected();

        if (isSelected) {
            // Se la musica è in pausa o fermata, esegui il fade in
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                fadeInMusic();  // Riprendi la musica con fade in
            }
        } else {
            fadeOutMusic();  // Metti in pausa la musica con fade out
        }
    }

    private void handleMusicToggle(boolean isSelected) {
        if (mediaPlayer != null) {
            // Controlla lo stato del MediaPlayer prima di avviare/fermare la musica
            if (isSelected) {
                if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    fadeInMusic();  // Avvia la musica con fade in
                }
            } else {
                if (mediaPlayer.getStatus() != MediaPlayer.Status.STOPPED && mediaPlayer.getStatus() != MediaPlayer.Status.PAUSED) {
                    fadeOutMusic();  // Ferma la musica con fade out
                }
            }
        }
    }


    private void handleMusicToggleOld(boolean isSelected) {
        if (mediaPlayer != null) {
            if (isSelected) {
                // Se selezionato, riprendi la riproduzione o riproduci dall'inizio
                mediaPlayer.play();  // Riprendi se è in pausa
            } else {
                // Se non selezionato, metti in pausa la riproduzione
                mediaPlayer.pause();  // Mette in pausa la musica senza ripartire da zero
            }
        }
    }
    private void fadeInMusic() {
        mediaPlayer.play();  // Riprendi la riproduzione dalla pausa
        mediaPlayer.setVolume(0.0);  // Inizia con il volume a 0

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(mediaPlayer.volumeProperty(), 0.0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(mediaPlayer.volumeProperty(), targetVolume))  // Durata del fade in
        );

        fadeIn.setOnFinished(event -> musicToggleButton.setDisable(false));  // Riabilita il pulsante dopo il fade in
        fadeIn.play();
    }

    private void fadeOutMusic() {
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(mediaPlayer.volumeProperty(), targetVolume)),
                new KeyFrame(Duration.seconds(2), new KeyValue(mediaPlayer.volumeProperty(), 0.0))  // Durata del fade out
        );

        // Quando il fade out è completato, metti in pausa la musica e riabilita il pulsante
        fadeOut.setOnFinished(event -> {
            mediaPlayer.pause();  // Metti in pausa la musica invece di fermarla
            musicToggleButton.setDisable(false);  // Riabilita il pulsante dopo la pausa

        });

        fadeOut.play();
    }



    @FXML
    private void handleModifyAction() {
        presenter.handleModifyAction();
    }

    private void setupEditModeButtons() {
        // Pulsante Annulla
        JFXButton cancelButton = new JFXButton("Annulla");
        cancelButton.setOnAction(e -> {
            presenter.cancelChanges();
            presenter.toggleEditMode(false);
        });

        // Pulsante Salva
        JFXButton saveButton = new JFXButton("Salva");
        saveButton.setOnAction(e -> {
            presenter.saveChanges();
            presenter.toggleEditMode(false);
        });

        // Aggiungi i pulsanti alla HBox
        editButtonsBox.getChildren().addAll(cancelButton, saveButton);
    }

    private void setupViewModeButtons() {
        // Pulsante Modifica
        JFXButton modifyButton = new JFXButton("Modifica");
        modifyButton.setOnAction(e -> presenter.handleModifyAction());
        editButtonsBox.getChildren().add(modifyButton);
    }

    private void renderApps(List<App> apps) {
        appsContainer.getChildren().clear();

        boolean isEditing = presenter.isEditing();

        for (App app : apps) {
            VBox appContainer = createAppContainer(app, isEditing);
            appsContainer.getChildren().add(appContainer);
        }
    }

    private VBox createAppContainer(App app, boolean isEditing) {
        VBox appContainer = new VBox(1);
        appContainer.setPadding(new Insets(10));
        //appContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        HBox appHeader = createAppHeader(app, isEditing);
        appContainer.getChildren().add(appHeader);

        if (!isEditing) {
            for (Host host : app.getHosts()) {
                HBox hostBox = new HBox(1);
                hostBox.setAlignment(Pos.CENTER_LEFT);

                Label ipLabel = new Label(host.getIp());
                Label fqdnLabel = new Label(host.getFqdn());
                ipLabel.setFont(new Font("Arial", 14));
                fqdnLabel.setFont(new Font("Arial", 14));

                JFXToggleButton toggleButton = new JFXToggleButton();
                toggleButton.setSelected(host.isEnabled());
                toggleButton.setText(host.isEnabled() ? "ON" : "OFF");

                // Aggiungi la classe CSS personalizzata
                toggleButton.getStyleClass().add("custom-jfx-toggle-button");

                toggleButton.setOnAction(e -> {
                    boolean newStatus = toggleButton.isSelected();
                    host.setEnabled(newStatus);
                    toggleButton.setText(newStatus ? "ON" : "OFF");
                    System.out.println((newStatus ? "Attivato " : "Disattivato ") + "IP: " + host.getIp() + " FQDN: " + host.getFqdn());
                });

                hostBox.getChildren().addAll(toggleButton, ipLabel, fqdnLabel);
                appContainer.getChildren().add(hostBox);
            }
        } else {
            for (Host host : app.getHosts()) {
                HBox hostBox = new HBox(1);
                hostBox.setAlignment(Pos.CENTER_LEFT);

                JFXToggleButton toggleButton = new JFXToggleButton();
                toggleButton.setSelected(host.isEnabled());
                toggleButton.setText(host.isEnabled() ? "ON" : "OFF");

                // Aggiungi la classe CSS personalizzata
                toggleButton.getStyleClass().add("custom-jfx-toggle-button");

                Label ipLabel = new Label(host.getIp());
                Label fqdnLabel = new Label(host.getFqdn());

                JFXButton deleteButton = new JFXButton("❌");
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(e -> {
                    presenter.removeHost(host);
                });

                hostBox.getChildren().addAll(toggleButton, ipLabel, fqdnLabel, deleteButton);
                appContainer.getChildren().add(hostBox);
            }

            JFXButton addButton = new JFXButton("Aggiungi Host");
            addButton.setOnAction(e -> System.out.println("Aggiungi nuovo record IP/FQDN."));
            appContainer.getChildren().add(addButton);
        }

        return appContainer;
    }

    private HBox createAppHeader(App app, boolean isEditing) {
        HBox appHeader = new HBox(1);
        appHeader.setAlignment(Pos.CENTER_LEFT);

        Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
        appLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

        appHeader.getChildren().add(appLabel);
        return appHeader;
    }

    private void showEditPane() {
        editPane.setVisible(true);
        editPane.setTranslateY(editPane.getHeight());

        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.5), editPane);
        slideUp.setFromY(editPane.getHeight());
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);
        slideUp.play();

        loadEditHosts();
    }

    private void hideEditPane() {
        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(0.5), editPane);
        slideDown.setFromY(0);
        slideDown.setToY(editPane.getHeight());
        slideDown.setInterpolator(Interpolator.EASE_IN);
        slideDown.setOnFinished(event -> editPane.setVisible(false));
        slideDown.play();
    }

    private void loadEditHosts() {
        List<App> apps = presenter.getApps();
        List<Host> allHosts = apps.stream()
                .flatMap(app -> app.getHosts().stream())
                .collect(java.util.stream.Collectors.toList());

        editHostListView.setItems(FXCollections.observableArrayList(allHosts));
        editHostListView.setCellFactory(param -> new HostEditCell(host -> {
            presenter.removeHost(host);
            editHostListView.getItems().remove(host);
        }));
    }

    @FXML
    private void handleSaveEdit() {
        presenter.saveChanges();
        toggleEditMode(false);
    }

    @FXML
    private void handleCancelEdit() {
        presenter.cancelChanges();
        toggleEditMode(false);
    }

    private void toggleEditMode(boolean isEditing) {
        presenter.toggleEditMode(isEditing);
    }

    private void animateButton(JFXButton button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setByX(1.2);
        st.setByY(1.2);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }
}