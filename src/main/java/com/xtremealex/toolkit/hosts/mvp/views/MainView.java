package com.xtremealex.toolkit.hosts.mvp.views;

import com.xtremealex.toolkit.hosts.models.App;
import java.util.List;

public interface MainView {
    void showMainContent();
    void showError(String message);
    void setApps(List<App> apps);
    void refreshApps();
    void setEditing(boolean isEditing);
}