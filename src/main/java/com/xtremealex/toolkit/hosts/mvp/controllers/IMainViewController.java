package com.xtremealex.toolkit.hosts.mvp.controllers;

import com.xtremealex.toolkit.hosts.models.App;
import java.util.List;

public interface IMainViewController {
    void setApps(List<App> apps);
    void refreshApps();
    void setEditing(boolean isEditing);

    void toggleEditMode(boolean isEditing);

    void showError(String message);
    void showInfo(String message);

    void showMainContent();

    void showNotification(String message);

    String askUserForHostsFilePath();
}