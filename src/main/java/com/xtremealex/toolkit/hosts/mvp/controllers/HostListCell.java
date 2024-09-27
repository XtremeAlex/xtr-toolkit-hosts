package com.xtremealex.toolkit.hosts.mvp.controllers;

import com.jfoenix.controls.JFXToggleButton;
import com.xtremealex.toolkit.hosts.models.Host;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class HostListCell extends ListCell<Host> {

    private HBox viewContent;
    private Label ipLabel;
    private Label fqdnLabel;
    private JFXToggleButton toggleButton;

    private Consumer<Host> onToggle;

    public HostListCell(boolean isEditing, Consumer<Host> onToggle) {
        super();
        this.onToggle = onToggle;

        ipLabel = new Label();
        fqdnLabel = new Label();
        toggleButton = new JFXToggleButton();
        toggleButton.getStyleClass().add("jfx-toggle-button");

        viewContent = new HBox(10, toggleButton, ipLabel, fqdnLabel);
        viewContent.setAlignment(Pos.CENTER_LEFT);

        toggleButton.setOnAction(e -> {
            Host host = getItem();
            if (host != null) {
                host.setEnabled(toggleButton.isSelected());
                onToggle.accept(host);
            }
        });
    }

    @Override
    protected void updateItem(Host host, boolean empty) {
        super.updateItem(host, empty);
        if (empty || host == null) {
            setGraphic(null);
        } else {
            ipLabel.setText(host.getIp());
            fqdnLabel.setText(host.getFqdn());
            toggleButton.setSelected(host.isEnabled());
            setGraphic(viewContent);
        }
    }
}