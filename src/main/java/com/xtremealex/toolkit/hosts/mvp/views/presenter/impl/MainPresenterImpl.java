package com.xtremealex.toolkit.hosts.mvp.views.presenter.impl;

import com.xtremealex.toolkit.hosts.IOHostParser;
import com.xtremealex.toolkit.hosts.mvp.views.presenter.IMainPresenter;
import com.xtremealex.toolkit.hosts.mvp.controllers.IMainViewController;
import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import com.xtremealex.toolkit.hosts.models.HostType;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MainPresenterImpl implements IMainPresenter {

    private final IMainViewController view;
    private boolean isEditing = false;
    private List<App> apps;
    private List<App> originalApps;
    private WatchService watchService;
    private String PATH_HOST = "";

    public MainPresenterImpl(IMainViewController view) {
        this.view = view;
    }

    @Override
    public void initialize() {
        try {
            // Determina il sistema operativo ed imposta il percorso del file hosts
            PATH_HOST = detectHostsFilePath();

            // Carica le app (implementa HostParser secondo le tue esigenze)
            apps = IOHostParser.parseHostsFile(PATH_HOST);
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
            startHostsFileWatcher();

        } catch (AccessDeniedException e) {
            //PROBLEMI DI PERMESSI
            e.printStackTrace();
            view.showError("Permessi insufficienti per accedere al file hosts. Si prega di eseguire l'applicazione con i permessi necessari.");
            selectHostsFileManually();

        } catch (NoSuchFileException e) {
            // FILE NON ESISTE
            e.printStackTrace();
            view.showError("File hosts non trovato. Si prega di selezionare il file manualmente.");
            selectHostsFileManually();

        } catch (IOException e) {
            // Errori di I/O
            e.printStackTrace();
            view.showError("Errore durante l'inizializzazione del file hosts: " + e.getMessage());

        } catch (Exception e) {
            // GENERICO
            e.printStackTrace();
            view.showError("Errore durante l'inizializzazione: " + e.getMessage());
        }
    }

    /**
     * Rileva il percorso del file hosts in base al sistema operativo.
     * @return Il percorso del file hosts.
     */
    private String detectHostsFilePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "C:/Windows/System32/drivers/etc/hosts";
        } else if (os.contains("mac")) {
            return "/etc/hosts";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "/etc/hosts";
        } else {
            throw new UnsupportedOperationException("Sistema operativo non supportato: " + os);
        }
    }

    /**
     * Permette all'utente di selezionare manualmente il file hosts.
     */
    private void selectHostsFileManually() {
        String selectedFilePath = view.askUserForHostsFilePath();
        if (selectedFilePath != null && !selectedFilePath.trim().isEmpty()) {
            PATH_HOST = selectedFilePath.trim();
            initialize();
        } else {
            view.showError("Percorso del file hosts non valido.");
        }
    }

    private void startHostsFileWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path hostsFilePath = Paths.get(PATH_HOST).getParent();
            hostsFilePath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Thread watcherThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            Path changed = (Path) event.context();
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY && changed.endsWith("hosts")) {
                                // Il file hosts è stato modificato
                                handleHostsFileChanged();
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            watcherThread.setDaemon(true);
            watcherThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleHostsFileChanged() {
        // Esegui l'aggiornamento sulla thread JavaFX
        Platform.runLater(() -> {
            // Mostra la notifica
            view.showNotification("Aggiornamento lista...");

            // Ricarica le app dal file hosts
            try {
                apps = IOHostParser.parseHostsFile(PATH_HOST);
                originalApps = deepCopyApps(apps);
                view.setApps(apps);
                view.refreshApps();
            } catch (IOException e) {
                e.printStackTrace();
                view.showError("Errore durante l'aggiornamento delle app: " + e.getMessage());
            }
        });
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
        if (!isEditing) {
            saveChangesAsync();
        }
    }

    @Override
    public void addHost(Host host, App app) {
        // Verifica se l'host esiste già
        boolean exists = app.getHosts().stream()
                .anyMatch(existingHost -> existingHost.getIp().equals(host.getIp())
                        && existingHost.getFqdn().equals(host.getFqdn()));
        if (!exists) {
            app.getHosts().add(host);
            view.refreshApps();
            if (!isEditing) {
                saveChangesAsync();
            }
        } else {
            view.showError("L'host esiste già.");
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
            if (!isEditing) {
                saveChangesAsync(); // Salva solo se non siamo in modalità modifica
            }
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
                if (updatedApp.equals(apps.get(i))) {
                    apps.set(i, updatedApp);
                    break;
                }
            }
            view.setApps(new ArrayList<>(apps));
            view.refreshApps();
            if (!isEditing) {
                saveChangesAsync();
            }
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
            if (!isEditing) {
                saveChangesAsync();
            }
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
            if (!isEditing) {
                saveChangesAsync();
            }
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
            if (!isEditing) {
                saveChangesAsync();
            }
        } else {
            view.showError("Non puoi eliminare l'App 'Unnamed App'.");
        }
    }

    @Override
    public void saveChanges() {
        try {
            String hostsFilePath = PATH_HOST;

            // salva sul fsystem
            IOHostParser.writeHostsFile(hostsFilePath, apps);

            // aggiorna lo stato originale delle app dopo il salvataggio
            originalApps = deepCopyApps(apps);

            // messaggio di conferma all'utente
            view.showInfo("Modifiche salvate con successo!");
        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Errore durante il salvataggio: " + e.getMessage());
        }
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

    /**
     * Recupera l'app associata a un determinato Host.
     *
     * @param host L'host per cui trovare l'app.
     * @return L'app associata all'host, o null se non trovata.
     */
    private App getAppForHost(Host host) {
        for (App app : apps) {
            if (app.getHosts().contains(host)) {
                return app;
            }
        }
        return null;
    }

    /**
     * Salva le modifiche in modo asincrono per evitare di bloccare l'interfaccia utente.
     */
    @Override
    public void saveChangesAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                String hostsFilePath = PATH_HOST;
                IOHostParser.writeHostsFile(hostsFilePath, new ArrayList<>(apps));
                originalApps = deepCopyApps(apps);

                // Questo l'ho tolto per essere piu user frendly
                // Platform.runLater(() -> view.showInfo("Modifiche salvate con successo!"));
            } catch (AccessDeniedException ade) {
                ade.printStackTrace();
                Platform.runLater(() -> view.showError("Permessi insufficienti per modificare il file hosts. Esegui l'applicazione come amministratore."));
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> view.showError("Errore durante il salvataggio: " + e.getMessage()));
            }
        });
    }

    public IMainViewController getView() {
        return view;
    }

}