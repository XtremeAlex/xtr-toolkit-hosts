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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
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
    @FXML private JFXToggleButton musicToggleButton;
    @FXML private JFXButton modifyButton; // Bottone "Modifica"
    @FXML private JFXButton cancelButton; // Bottone "Annulla"
    @FXML private JFXButton saveButton; // Bottone "Salva"

    private MainPresenter presenter;
    private MediaPlayer mediaPlayer;
    private final double targetVolume = 0.1;  // Volume massimo desiderato

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        presenter = new MainPresenterImpl(this);
        presenter.initialize();

        setupInitialVisibility();
        startSplashScreenTransition();
    }

    /**
     * Imposta la visibilità iniziale degli elementi.
     */
    private void setupInitialVisibility() {
        splashScreen.setVisible(true);
        mainContent.setVisible(false);
        // Inizialmente, nascondi i bottoni "Annulla" e "Salva"
        cancelButton.setVisible(false);
        saveButton.setVisible(false);
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
        //mainContent.setOpacity(10);

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
            fadeInMusic();

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
        } else {
            setupViewModeButtons();
            modifyButton.setVisible(true);   // Mostra il bottone "Modifica"
            cancelButton.setVisible(false);  // Nascondi "Annulla"
            saveButton.setVisible(false);    // Nascondi "Salva"
        }

        refreshApps();
    }

    /**
     * Gestisce l'azione del bottone "Modifica".
     *
     * @param event L'evento di azione.
     */
    @FXML
    private void handleModifyAction(ActionEvent event) {
        presenter.handleModifyAction();
    }

    /**
     * Configura i bottoni per la modalità di modifica.
     * In questo caso, i bottoni sono già presenti nello FXML e vengono solo resi visibili.
     */
    private void setupEditModeButtons() {
        // Nessuna azione aggiuntiva necessaria poiché "Annulla" e "Salva" sono già visibili
    }

    /**
     * Configura i bottoni per la modalità di visualizzazione.
     * In questo caso, i bottoni "Annulla" e "Salva" vengono nascosti.
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

        HBox appHeader = createAppHeader(app, isEditing);
        appContainer.getChildren().add(appHeader);

        if (!isEditing) {
            for (Host host : app.getHosts()) {
                HBox hostBox = createHostBox(host);
                appContainer.getChildren().add(hostBox);
            }
        } else {
            for (Host host : app.getHosts()) {
                HBox hostBox = createEditableHostBox(host);
                appContainer.getChildren().add(hostBox);
            }

            // Pulsante "Aggiungi Host"
            JFXButton addButton = new JFXButton("Aggiungi Host");
            addButton.setOnAction(e -> {
                Host newHost = new Host("Nuovo IP", "Nuovo FQDN", true);
                presenter.addHost(newHost);
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

        Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
        appLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

        appHeader.getChildren().add(appLabel);
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
        ipLabel.setFont(new Font("Arial", 14));
        fqdnLabel.setFont(new Font("Arial", 14));

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
     * @return L'HBox contenente l'host modificabile.
     */
    private HBox createEditableHostBox(Host host) {
        HBox hostBox = new HBox(10);
        hostBox.setAlignment(Pos.CENTER_LEFT);

        // Campo di testo per IP
        TextField ipField = new TextField(host.getIp());
        ipField.setPrefWidth(150);

        // Campo di testo per FQDN
        TextField fqdnField = new TextField(host.getFqdn());
        fqdnField.setPrefWidth(150);

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
        return hostBox;
    }

    /**
     * Gestisce l'azione di salvataggio delle modifiche.
     */
    @FXML
    private void handleSaveEdit() {
        // Validazione dei dati prima del salvataggio
        for (App app : presenter.getApps()) {
            for (Host host : app.getHosts()) {
                if (!isValidIP(host.getIp())) {
                    showError("IP non valido: " + host.getIp());
                    return;
                }
                if (!isValidFQDN(host.getFqdn())) {
                    showError("FQDN non valido: " + host.getFqdn());
                    return;
                }
            }
        }

        presenter.saveChanges();
        toggleEditMode(false);
    }

    /**
     * Gestisce l'azione di annullamento delle modifiche.
     */
    @FXML
    private void handleCancelEdit() {
        presenter.cancelChanges();
        toggleEditMode(false);
    }

    /**
     * Alterna la modalità di modifica.
     *
     * @param isEditing Se abilitare la modalità di modifica.
     */
    private void toggleEditMode(boolean isEditing) {
        presenter.toggleEditMode(isEditing);
    }

    /**
     * Anima un bottone con una transizione di scala.
     *
     * @param button Il bottone da animare.
     */
    private void animateButton(JFXButton button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setByX(1.2);
        st.setByY(1.2);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
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
     * Implementa il metodo per mostrare gli errori all'utente.
     *
     * @param message Il messaggio di errore da mostrare.
     */
    @Override
    public void showError(String message) {
        // Utilizza un dialogo di JavaFX per mostrare gli errori
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Implementa il metodo per mostrare il contenuto principale.
     */
    @Override
    public void showMainContent() {
        mainContent.setVisible(true);
    }
}