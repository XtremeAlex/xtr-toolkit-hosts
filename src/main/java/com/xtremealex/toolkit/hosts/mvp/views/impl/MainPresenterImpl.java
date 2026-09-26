package com.xtremealex.toolkit.hosts.mvp.views.impl;

import com.xtremealex.toolkit.hosts.HostParser;
import com.xtremealex.toolkit.hosts.mvp.views.MainPresenter;
import com.xtremealex.toolkit.hosts.mvp.views.MainView;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;

import java.io.IOException;
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
            if (apps.isEmpty()) {
                // Crea un'app predefinita se il file hosts è vuoto
                App defaultApp = new App("Default App", "Informazioni di default", HostType.IP, "lb1", new ArrayList<>(), true);
                apps.add(defaultApp);
            }
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
    public void addHost(Host host) {
        if (apps.isEmpty()) {
            // Crea un'app predefinita se non ce ne sono
            App defaultApp = new App("Default App", "Informazioni di default", HostType.IP, "lb1", new ArrayList<>(), true);
            apps.add(defaultApp);
        }
        apps.get(0).getHosts().add(host); // Aggiungi all'app predefinita
        view.setApps(apps);
        view.refreshApps();
    }

    @Override
    public void saveChanges() {
        /*
        try {
            HostParser.writeHostsFile("/etc/hosts", apps);
            originalApps = deepCopyApps(apps);
            view.showInfo("Modifiche salvate con successo!");
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

    @Override
    public void addApp(App app) {
        apps.add(app);
        view.setApps(apps);
        view.refreshApps();
    }

    @Override
    public void removeApp(App app) {
        apps.remove(app);
        view.setApps(apps);
        view.refreshApps();
    }

    @Override
    public void updateApp(App app) {
        // Poiché le App sono oggetti mutabili, non è necessario fare nulla qui.
        // Tuttavia, puoi salvare lo stato se necessario.
        view.setApps(apps);
        view.refreshApps();
    }

}