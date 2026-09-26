package old;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXToggleButton;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.mvp.MusicPlayer;
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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainControllerMUSIC implements Initializable, MainView {

    @FXML private StackPane rootPane;
    @FXML private Pane splashScreen;
    @FXML private AnchorPane mainContent;
    @FXML private VBox appsContainer;
    @FXML private HBox editButtonsBox;
    @FXML private JFXToggleButton musicToggleButton;
    @FXML private JFXListView<Host> editHostListView;

    private MainPresenter presenter;
    private MusicPlayer musicPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        presenter = new MainPresenterImpl(this);
        presenter.initialize();

        // Setup iniziale della visibilità
        setupInitialVisibility();

        // Avvia la transizione dello splash screen
        startSplashScreenTransition();

        // Inizializza il player musicale con il percorso della musica
        musicPlayer = new MusicPlayer("/music/background.wav");

        // Disabilita il pulsante fino a quando la transizione non è completa
        musicPlayer.setOnTransitionComplete(() -> musicToggleButton.setDisable(false));

        // Se il pulsante è selezionato all'avvio, avvia la musica
        if (musicToggleButton.isSelected()) {
            musicPlayer.playMusic();
        }

        // Collega il comportamento del pulsante al player musicale
        bindMusicToggleButton();
    }


    private void bindMusicToggleButton() {
        musicToggleButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            handleMusicToggle(isNowSelected);
        });
    }

    private void handleMusicToggle(boolean isSelected) {
        musicToggleButton.setDisable(true);  // Disabilita subito il pulsante

        if (isSelected) {
            if (musicPlayer.isReady()) {
                musicPlayer.playMusic();
            }
        } else {
            musicPlayer.pauseMusic();
        }
    }

    @FXML
    private void handleMusicToggle(ActionEvent event) {
        musicToggleButton.setDisable(true);  // Disabilita temporaneamente il pulsante per evitare clic ripetuti

        boolean isSelected = musicToggleButton.isSelected();
        if (isSelected) {
            musicPlayer.playMusic();  // Riprendi la musica
        } else {
            musicPlayer.pauseMusic();  // Metti in pausa la musica
        }

        musicToggleButton.setDisable(false);  // Riabilita il pulsante dopo la transizione
    }

    @Override
    public void showMainContent() {
        mainContent.setVisible(true);
    }

    @Override
    public void showError(String message) {
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
        editButtonsBox.getChildren().clear();
        if (isEditing) {
            setupEditModeButtons();
        } else {
            setupViewModeButtons();
        }
        refreshApps();
    }



    private void setupInitialVisibility() {
        splashScreen.setVisible(true);
        mainContent.setVisible(false);
        //editPane.setVisible(false); // Assicurati che l'editPane sia nascosto all'inizio
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
            //initializeBackgroundMusic();
        });

        transition.play();
    }


    private void setupEditModeButtons() {
        JFXButton cancelButton = new JFXButton("Annulla");
        cancelButton.setOnAction(e -> {
            presenter.cancelChanges();
            presenter.toggleEditMode(false);
        });

        JFXButton saveButton = new JFXButton("Salva");
        saveButton.setOnAction(e -> {
            presenter.saveChanges();
            presenter.toggleEditMode(false);
        });

        editButtonsBox.getChildren().addAll(cancelButton, saveButton);
    }

    private void setupViewModeButtons() {
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
        VBox appContainer = new VBox(10);
        appContainer.setPadding(new Insets(10));
        appContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        HBox appHeader = createAppHeader(app, isEditing);
        appContainer.getChildren().add(appHeader);

        if (!isEditing) {
            for (Host host : app.getHosts()) {
                HBox hostBox = new HBox(10);
                hostBox.setAlignment(Pos.CENTER_LEFT);

                Label ipLabel = new Label(host.getIp());
                Label fqdnLabel = new Label(host.getFqdn());
                ipLabel.setFont(new Font("Arial", 14));
                fqdnLabel.setFont(new Font("Arial", 14));

                JFXToggleButton toggleButton = new JFXToggleButton();
                toggleButton.setSelected(host.isEnabled());
                toggleButton.setText(host.isEnabled() ? "ON" : "OFF");

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
                HBox hostBox = new HBox(10);
                hostBox.setAlignment(Pos.CENTER_LEFT);

                JFXToggleButton toggleButton = new JFXToggleButton();
                toggleButton.setSelected(host.isEnabled());
                toggleButton.setText(host.isEnabled() ? "ON" : "OFF");

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
        HBox appHeader = new HBox(10);
        appHeader.setAlignment(Pos.CENTER_LEFT);

        Label appLabel = new Label(app.getName() != null ? app.getName() : "Unnamed App");
        appLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");

        appHeader.getChildren().add(appLabel);
        return appHeader;
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