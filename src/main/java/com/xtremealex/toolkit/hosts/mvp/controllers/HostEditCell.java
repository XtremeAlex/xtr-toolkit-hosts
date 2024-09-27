package com.xtremealex.toolkit.hosts.mvp.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.xtremealex.toolkit.hosts.models.Host;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class HostEditCell extends ListCell<Host> {

    private HBox content;
    private JFXTextField ipField;
    private JFXTextField fqdnField;
    private JFXButton deleteButton;

    private Consumer<Host> onDelete;

    public HostEditCell(Consumer<Host> onDelete) {
        super();
        this.onDelete = onDelete;

        ipField = new JFXTextField();
        ipField.setPromptText("IP");
        fqdnField = new JFXTextField();
        fqdnField.setPromptText("FQDN");
        deleteButton = new JFXButton("Elimina");
        deleteButton.getStyleClass().add("delete-button");

        content = new HBox(10, ipField, fqdnField, deleteButton);
        content.setAlignment(Pos.CENTER_LEFT);

        deleteButton.setOnAction(e -> {
            Host host = getItem();
            if (host != null) {
                onDelete.accept(host);
            }
        });

        // Aggiorna i campi quando l'elemento cambia
        ipField.textProperty().addListener((obs, oldText, newText) -> {
            if (getItem() != null) {
                getItem().setIp(newText);
            }
        });

        fqdnField.textProperty().addListener((obs, oldText, newText) -> {
            if (getItem() != null) {
                getItem().setFqdn(newText);
            }
        });
    }

    @Override
    protected void updateItem(Host host, boolean empty) {
        super.updateItem(host, empty);
        if (empty || host == null) {
            setGraphic(null);
        } else {
            ipField.setText(host.getIp());
            fqdnField.setText(host.getFqdn());
            setGraphic(content);
        }
    }
}