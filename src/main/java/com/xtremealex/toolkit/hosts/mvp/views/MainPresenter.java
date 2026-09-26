package com.xtremealex.toolkit.hosts.mvp.views;

import com.xtremealex.toolkit.hosts.models.App;
import com.xtremealex.toolkit.hosts.models.Host;
import java.util.List;

public interface MainPresenter {
    void initialize();
    List<App> getApps();
    boolean isEditing();
    void toggleEditMode(boolean isEditing);
    void handleModifyAction();
    void removeHost(Host host);
    void saveChanges();
    void cancelChanges();
}