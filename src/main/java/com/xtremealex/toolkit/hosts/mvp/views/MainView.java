package com.xtremealex.toolkit.hosts.mvp.views;

import com.xtremealex.toolkit.hosts.models.App;
import java.util.List;

public interface MainView {
    void setApps(List<App> apps);
    void refreshApps();
    void setEditing(boolean isEditing);

    void toggleEditMode(boolean isEditing);

    void showError(String message);
    void showInfo(String message);

    void showMainContent();
}