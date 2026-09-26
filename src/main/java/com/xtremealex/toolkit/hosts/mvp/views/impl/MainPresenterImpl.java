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
                App defaultApp = new App("Unnamed App", "Informazioni di default", HostType.IP, null, new ArrayList<>(), true);
                apps.add(defaultApp);
            }
            originalApps = deepCopyApps(apps);

            view.setApps(apps);
            view.refreshApps();

            // Mostra il contenuto principale se necessario
            view.showMainContent();

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
    public void addHost(Host host, App app) {
        if (app != null) {
            app.getHosts().add(host);
            view.setApps(apps);
            view.refreshApps();
        } else {
            view.showError("App non valida per aggiungere un host.");
        }
    }

    @Override
    public void addHost(Host host) {
        if (!apps.isEmpty()) {
            App lastApp = apps.get(apps.size() - 1);
            lastApp.getHosts().add(host);
            view.setApps(apps);
            view.refreshApps();
        } else {
            view.showError("Nessuna App disponibile per aggiungere un host.");
        }
    }

    @Override
    public void addApp(App app) {
        if (app != null) {
            if (app.getName() == null || app.getName().trim().isEmpty()) {
                app.setName("Unnamed App");
            }
            apps.add(app);
            view.setApps(apps);
            view.refreshApps();
        } else {
            view.showError("App non valida.");
        }
    }

    @Override
    public void updateApp(App updatedApp) {
        if (updatedApp != null) {
            if (updatedApp.getName() == null || updatedApp.getName().trim().isEmpty()) {
                updatedApp.setName("Unnamed App");
            }
            for (int i = 0; i < apps.size(); i++) {
                if (apps.get(i).getName().equals(updatedApp.getName())) {
                    apps.set(i, updatedApp);
                    break;
                }
            }
            view.setApps(apps);
            view.refreshApps();
        } else {
            view.showError("App non valida.");
        }
    }

    @Override
    public void updateHost(Host updatedHost, App app) {
        if (app != null && updatedHost != null) {
            List<Host> hosts = app.getHosts();
            for (int i = 0; i < hosts.size(); i++) {
                Host host = hosts.get(i);
                if (host.getFqdn().equals(updatedHost.getFqdn())) {
                    hosts.set(i, updatedHost);
                    break;
                }
            }
            view.setApps(apps);
            view.refreshApps();
        } else {
            view.showError("App o Host non valido per l'aggiornamento.");
        }
    }

    @Override
    public void updateHost(Host updatedHost) {
        if (updatedHost != null) {
            for (App app : apps) {
                List<Host> hosts = app.getHosts();
                for (int i = 0; i < hosts.size(); i++) {
                    Host host = hosts.get(i);
                    if (host.getFqdn().equals(updatedHost.getFqdn())) {
                        hosts.set(i, updatedHost);
                        break;
                    }
                }
            }
            view.setApps(apps);
            view.refreshApps();
        } else {
            view.showError("Host non valido.");
        }
    }

    @Override
    public void removeApp(App app) {
        if (app != null && !app.getName().equalsIgnoreCase("Unnamed App")) {
            apps.remove(app);
            view.setApps(apps);
            view.refreshApps();
        } else {
            view.showError("Non puoi eliminare l'App 'Unnamed App'.");
        }
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

    @Override
    public void showMainContent() {
        view.showMainContent();
    }

    /**
     * Crea una copia profonda delle App.
     *
     * @param apps Le App da copiare.
     * @return Una nuova lista di App.
     */
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