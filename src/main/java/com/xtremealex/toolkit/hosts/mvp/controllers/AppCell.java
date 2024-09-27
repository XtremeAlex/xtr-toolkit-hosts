package com.xtremealex.toolkit.hosts.mvp.controllers;

import com.jfoenix.controls.JFXButton;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.mvp.controllers.impl.MainViewControllerImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AppCell extends ListCell<App> {

    private VBox appContainer;
    private GridPane appHeader;
    private Label appLabel;

    private VBox hostsContainer;
    private JFXButton addHostButton;
    private MainViewControllerImpl mainController;

    public AppCell(MainViewControllerImpl mainController) {
        this.mainController = mainController;
        createCellContent();
    }

    private void createCellContent() {
        // Spacing aumentato vedere meglio
        appContainer = new VBox(10);
        appContainer.setPadding(new Insets(10));
        appContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");

        appHeader = new GridPane();
        appHeader.setHgap(10);
        appHeader.setVgap(5);
        appHeader.setAlignment(Pos.CENTER_LEFT);

        // Configura le colonne del GridPane
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.NEVER);
        col1.setPrefWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setPrefWidth(200);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.NEVER);
        col3.setPrefWidth(50);

        appHeader.getColumnConstraints().addAll(col1, col2, col3);

        appLabel = new Label();
        appLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333;");
        // add appLabel alla prima riga, spanning 2 colonne
        appHeader.add(appLabel, 0, 0, 2, 1);

        hostsContainer = new VBox(5);
        hostsContainer.setPadding(new Insets(10, 0, 10, 20));

        addHostButton = new JFXButton("Aggiungi Host");
        addHostButton.setPrefWidth(150);
        // Inizialmente nascosto
        addHostButton.setVisible(false);
        addHostButton.setOnAction(e -> {
            Host newHost = new Host("Nuovo IP", "Nuovo FQDN", true);
            mainController.getPresenter().addHost(newHost, getItem());
        });

        appContainer.getChildren().addAll(appHeader, hostsContainer, addHostButton);
    }

    @Override
    protected void updateItem(App app, boolean empty) {
        super.updateItem(app, empty);

        if (empty || app == null) {
            setText(null);
            setGraphic(null);
        } else {
            // testo dell'app
            appLabel.setText(app.getName() != null ? app.getName() : "Unnamed App");

            // Pulisci appHeader e hostsContainer
            appHeader.getChildren().clear();
            hostsContainer.getChildren().clear();

            boolean isEditing = mainController.getPresenter().isEditing();

            if (isEditing) {
                // Mostra il pulsante per aggiungere host
                addHostButton.setVisible(true);

                // testo per Nome App
                TextField appNameField = new TextField(app.getName() != null ? app.getName() : "Unnamed App");
                appNameField.setPrefWidth(200);
                appNameField.textProperty().addListener((obs, oldText, newText) -> {
                    app.setName(newText.trim().isEmpty() ? "Unnamed App" : newText.trim());
                });

                JFXButton deleteAppButton = new JFXButton("❌");
                deleteAppButton.getStyleClass().add("delete-button");
                deleteAppButton.setOnAction(e -> {
                    mainController.getModalController().openDeleteAppModal(app);
                });

                // Aggiungi Nome App e pulsante "X"
                appHeader.add(new Label("Nome App:"), 0, 0);
                appHeader.add(appNameField, 1, 0);
                appHeader.add(deleteAppButton, 2, 0);

                // Campo di testo per Load Balancer (LB) o pulsante per aggiungerne uno
                if (app.getLb() != null && !app.getLb().isEmpty()) {
                    TextField lbField = new TextField(app.getLb());
                    lbField.setPrefWidth(300);
                    lbField.setPromptText("Load Balancer");
                    lbField.textProperty().addListener((obs, oldText, newText) -> {
                        app.setLb(newText.trim().isEmpty() ? null : newText.trim());
                    });

                    // "Elimina Load Balancer"
                    JFXButton deleteLbButton = new JFXButton("❌");
                    deleteLbButton.getStyleClass().add("delete-button");
                    deleteLbButton.setOnAction(e -> {
                        mainController.getModalController().openRemoveLbModal(app);
                    });

                    HBox lbBox = new HBox(5, lbField, deleteLbButton);
                    lbBox.setAlignment(Pos.CENTER_LEFT);

                    appHeader.add(new Label("Load Balancer:"), 0, 1);
                    //appHeader.add(lbBox, 1, 1);
                    appHeader.add(lbField, 1, 1);
                    appHeader.add(deleteLbButton, 2, 1);

                } else {
                    // "Aggiungi Load Balancer"
                    JFXButton addLbButton = new JFXButton("Aggiungi Load Balancer");
                    addLbButton.setPrefWidth(500);
                    addLbButton.setOnAction(e -> {
                        mainController.getModalController().openAddLbModal(app);
                    });
                    appHeader.add(new Label("Load Balancer:"), 0, 1);
                    appHeader.add(addLbButton, 1, 1);
                }

                // host modificabili
                for (Host host : app.getHosts()) {
                    HBox hostBox = mainController.createEditableHostBox(host, app);
                    hostsContainer.getChildren().add(hostBox);
                }
            } else {
                // Nascondi il pulsante per aggiungere host
                addHostButton.setVisible(false);

                // add solo appLabel, spanning 2 colonne
                appHeader.add(appLabel, 0, 0, 2, 1);

                // Mostra il Load Balancer se presente, su una nuova riga
                if (app.getLb() != null && !app.getLb().isEmpty()) {
                    Label lbLabel = new Label("LB: " + app.getLb());
                    lbLabel.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
                    // Spanning 3 colonne per poterlo allineare sotto Nome App
                    appHeader.add(lbLabel, 0, 1, 3, 1);
                }

                // Aggiungi host non modificabili
                for (Host host : app.getHosts()) {
                    HBox hostBox = mainController.createHostBox(host);
                    hostsContainer.getChildren().add(hostBox);
                }
            }

            setGraphic(appContainer);
        }
    }
}