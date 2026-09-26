package com.xtremealex.toolkit.hosts.mvp.controllers.impl;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.mvp.controllers.IMainViewController;
import com.xtremealex.toolkit.hosts.mvp.controllers.ModalController;
import com.xtremealex.toolkit.hosts.mvp.controllers.AppCell;
import com.xtremealex.toolkit.hosts.mvp.views.presenter.impl.MainPresenterImpl;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainViewControllerImpl implements Initializable, IMainViewController {

    /**
     * FXML
     */
    @FXML
    private StackPane rootPane;
    @FXML
    private Pane splashScreen;
    @FXML
    private AnchorPane mainContent;
    @FXML
    private ListView<App> appsListView;
    @FXML
    private JFXToggleButton musicToggleButton;
    @FXML
    private JFXButton modifyButton;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private JFXButton saveButton;
    @FXML
    private JFXButton addAppButton;

    /**
     * Oggetti in comune
     */
    private MainPresenterImpl presenter;
    private MediaPlayer mediaPlayer;
    private final double targetVolume = 0.1;

    private ModalController modalController;

    private Label notificationLabel;

    /**
     * INIT
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        presenter = new MainPresenterImpl(this);
        presenter.initialize();

        // Inizializza il ModalController
        modalController = new ModalController(presenter);

        setupInitialVisibility();
        startSplashScreenTransition();
        setupListView();

        //Inserisco la notifica in alto quando qualcuno modifica il file, sempre in ascolto sul file
        notificationLabel = new Label();
        notificationLabel.setStyle("-fx-background-color:  #000000; -fx-text-fill: #40e740;");
        notificationLabel.setVisible(false);
        rootPane.getChildren().add(notificationLabel);
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
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(targetVolume);
            mediaPlayer.play();

            // Serve a sincronizza lo stato del toggle con il MediaPlayer
            musicToggleButton.setSelected(true);

            // Listener per sincronizzare lo stato del toggle con il MediaPlayer, per evitare che la musica parta piu volte
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
     * Unisce il pulsante toggle della musica con il MediaPlayer.
     */
    private void bindMusicToggleButton() {
        musicToggleButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (mediaPlayer != null) {
                handleMusicToggle(isNowSelected);
            }
        });
    }

    /**
     * Gestisce ON/OFF della musica.
     * @param isSelected Se la musica deve essere attivata.
     */
    private void handleMusicToggle(boolean isSelected) {
        if (isSelected) {
            //ON
            fadeInMusic();
        } else {
            //OFF
            fadeOutMusic();
        }
    }

    private void fadeInMusic() {
        if (mediaPlayer == null) return;
        // questo disabilita il toggle durante la transizione
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

    private void fadeOutMusic() {
        if (mediaPlayer == null) return;
        // questo disabilita il toggle durante la transizione
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

    /**
     * Configura la ListView per le app con un CellFactory per ridurre il carico sul hardware
     */
    private void setupListView() {
        appsListView.setCellFactory(param -> new AppCell(MainViewControllerImpl.this));
        appsListView.setFocusTraversable(false);
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
            modifyButton.setDisable(true);
            //modifyButton.setVisible(false);
            cancelButton.setVisible(true);
            saveButton.setVisible(true);
            addAppButton.setVisible(true);
        } else {
            setupViewModeButtons();
            modifyButton.setDisable(false);
            modifyButton.setVisible(true);
            cancelButton.setVisible(false);
            saveButton.setVisible(false);
            addAppButton.setVisible(false);
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
     * I pulsanti sono già presenti nello FXML e vengono solo resi visibili.
     */
    private void setupEditModeButtons() {
        // NON NECCESSARIO poiché "Annulla" e "Salva" sono già visibili
    }

    /**
     * Configura i bottoni per la modalità di visualizzazione.
     * In questo caso, i pulsanti "Annulla" e "Salva" vengono nascosti.
     */
    private void setupViewModeButtons() {
        // NON NECCESSARIO poiché i bottoni vengono nascosti nel setEditing
    }

    /**
     * Renderizza le app nella GUI.
     *
     * @param apps La lista di app da rendere.
     */
    private void renderApps(List<App> apps) {
        appsListView.getItems().clear();
        appsListView.getItems().addAll(apps);
    }

    /**
     * Gestione dei messaggi di errore
     * @param message Il messaggio da mostrare.
     */
    @Override
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gestione dei messaggi di info
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
     * "Aggiungi App".
     *
     * @param event L'evento di azione.
     */
    @FXML
    private void handleAddAppAction(ActionEvent event) {
        modalController.openAddAppModal();
    }

    /**
     *  "Salva".
     *
     * @param event L'evento di azione.
     */
    @FXML
    private void handleSaveEdit(ActionEvent event) {
        presenter.saveChanges();
        toggleEditMode(false);
    }

    /**
     * "Annulla".
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

    // Getter per il presenter (necessario nella AppCell)
    public MainPresenterImpl getPresenter() {
        return presenter;
    }

    // Getter per il ModalController (necessario nella AppCell)
    public ModalController getModalController() {
        return modalController;
    }

    /**
     * Crea un HBox per visualizzare un host in modalità di visualizzazione.
     *
     * @param host L'host da visualizzare.
     * @return L'HBox contenente l'host.
     */
    public HBox createHostBox(Host host) {
        HBox hostBox = new HBox(10);
        hostBox.setAlignment(Pos.CENTER_LEFT);

        JFXToggleButton toggleButton = new JFXToggleButton();
        toggleButton.setSelected(host.isEnabled());
        toggleButton.setText(host.isEnabled() ? "ON" : "OFF");
        toggleButton.getStyleClass().add("custom-jfx-toggle-button");

        Label ipLabel = new Label(host.getIp());
        Label fqdnLabel = new Label(host.getFqdn());
        ipLabel.setFont(new Font("Arial", 14));
        fqdnLabel.setFont(new Font("Arial", 14));

        toggleButton.setOnAction(e -> {
            boolean newStatus = toggleButton.isSelected();
            host.setEnabled(newStatus);
            toggleButton.setText(newStatus ? "ON" : "OFF");
            // Chiamata al presenter per salvare le modifiche, questo è un metodo asincrono per evitare blocchi UI
            presenter.saveChangesAsync();
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
    public HBox createEditableHostBox(Host host, App app) {
        HBox hostBox = new HBox(10);
        hostBox.setAlignment(Pos.CENTER_LEFT);

        // IP
        TextField ipField = new TextField(host.getIp());
        ipField.setPrefWidth(150);
        ipField.textProperty().addListener((obs, oldText, newText) -> {
            host.setIp(newText.trim());
        });

        //FQDN
        TextField fqdnField = new TextField(host.getFqdn());
        fqdnField.setPrefWidth(235);
        fqdnField.textProperty().addListener((obs, oldText, newText) -> {
            host.setFqdn(newText.trim());
        });

        //"Elimina"
        JFXButton deleteButton = new JFXButton("❌");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> {
            presenter.removeHost(host);
        });

        //"Aggiorna IP"
        JFXButton updateIpButton = null;
        if (app.getLb() != null && !app.getLb().isEmpty()) {
            updateIpButton = new JFXButton("Aggiorna IP");
            updateIpButton.setOnAction(e -> {
                modalController.openUpdateIpModal(host, app.getLb());
            });
        }

        if (updateIpButton != null) {
            hostBox.getChildren().addAll(ipField, fqdnField, deleteButton, updateIpButton);
        } else {
            hostBox.getChildren().addAll(ipField, fqdnField, deleteButton);
        }

        return hostBox;
    }


    @Override
    public void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);

        // Notifica al centro o in alto
        StackPane.setAlignment(notificationLabel, Pos.TOP_CENTER);

        // Nascondi la notifica dopo 3 secondi, animazione blanda...
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(3),
                ae -> notificationLabel.setVisible(false)));
        timeline.play();
    }

    @Override
    public String askUserForHostsFilePath() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona il file hosts");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File hosts", "hosts"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        }
        return null;
    }
}