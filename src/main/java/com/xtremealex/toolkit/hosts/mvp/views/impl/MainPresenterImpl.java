package com.xtremealex.toolkit.hosts.mvp.views.impl;

import com.xtremealex.toolkit.hosts.HostParser;
import com.xtremealex.toolkit.hosts.mvp.views.MainPresenter;
import com.xtremealex.toolkit.hosts.mvp.views.MainView;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainPresenterImpl implements MainPresenter {

    private final MainView view;
    private boolean isEditing = false;
    private List<App> apps;
    private List<App> originalApps;

    public MainPresenterImpl(MainView view) {
        this.view = view;
    }

    @Override
    public void initialize() {
        try {
            // Carica le app (implementa HostParser secondo le tue esigenze)
            apps = HostParser.parseHostsFile("/etc/hosts");
            originalApps = deepCopyApps(apps);

            view.setApps(apps);
            view.refreshApps();

        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Errore durante l'inizializzazione: " + e.getMessage());
        }
    }

    @Override
    public List<App> getApps() {
        return apps;
    }

    @Override
    public boolean isEditing() {
        return isEditing;
    }

    @Override
    public void toggleEditMode(boolean isEditing) {
        this.isEditing = isEditing;
        view.setEditing(isEditing);
        view.refreshApps();
    }

    @Override
    public void handleModifyAction() {
        toggleEditMode(true);
    }

    @Override
    public void removeHost(Host host) {
        for (App app : apps) {
            if (app.getHosts().contains(host)) {
                app.getHosts().remove(host);
                break;
            }
        }
        view.setApps(apps);
        view.refreshApps();
    }

    @Override
    public void saveChanges() {
        /*
        try {
            HostParser.writeHostsFile("/etc/hosts", apps);
            originalApps = deepCopyApps(apps);
            view.showError("Modifiche salvate con successo!");
        } catch (IOException e) {
            e.printStackTrace();
            view.showError("Errore durante il salvataggio: " + e.getMessage());
        }
        */

    }

    @Override
    public void cancelChanges() {
        apps = deepCopyApps(originalApps);
        view.setApps(apps);
        view.refreshApps();
    }

    private List<App> deepCopyApps(List<App> apps) {
        return apps.stream()
                .map(app -> new App(
                        app.getName(),
                        app.getInfo(),
                        app.getHostType(),
                        app.getLb(),
                        new ArrayList<>(app.getHosts()),
                        app.isAutoload()))
                .collect(Collectors.toList());
    }
}