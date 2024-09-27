package com.xtremealex.toolkit.hosts.mvp.views.presenter;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;

import java.util.List;

public interface IMainPresenter {
    void initialize();
    List<App> getApps();
    boolean isEditing();
    void toggleEditMode(boolean isEditing);
    void handleModifyAction();
    void removeHost(Host host);
    void addHost(Host host, App app);
    void addHost(Host host);
    void addApp(App app);
    void updateApp(App updatedApp);
    void updateHost(Host host, App app);
    void updateHost(Host updatedHost);
    void removeApp(App app);
    void saveChanges();
    void cancelChanges();
    void showMainContent(); // Aggiunto se necessario

    void saveChangesAsync();
}