/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.codewind.intellij.ui.toolwindow;

import com.intellij.ui.content.Content;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.IUpdateHandler;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Generic Update Handler for any Tool Windows (Overview window, Logs window, Codewind view...)
 */
public class UpdateHandler implements IUpdateHandler
{
    private static final UpdateHandler INSTANCE = new UpdateHandler();

    public static UpdateHandler getInstance() {
        return INSTANCE;
    }
    private UpdateHandler() {
        // Empty
    }

    private static final HashMap<AppKey, AppUpdateListener> appListeners = new HashMap<AppKey, AppUpdateListener>();

    @Override
    public void updateAll() {
        updateApps();
    }

    @Override
    public void updateConnection(CodewindConnection connection) {
        updateApps(connection);
    }

    private void updateApps() {
        ConnectionManager.getManager().activeConnections().forEach(this::updateApps);
    }

    private void updateApps(CodewindConnection conn) {
        if (conn != null) {
            conn.getApps().forEach(this::updateApplication);
        }
    }

    @Override
    public void updateApplication(CodewindApplication application) {
        synchronized(appListeners) {
            Set<AppKey> appKeys = appListeners.keySet();
            for (AppKey key : appKeys) {
                if (application.connection.getConid().equals(key.connectionId) &&
                        application.projectID.equals(key.projectId)) {
                    AppUpdateListener listener = appListeners.get(key);
                    if (listener != null) {
                        listener.update();
                    }
                }
            }
        }
    }

    @Override
    public void removeConnection(List<CodewindApplication> apps) {
        synchronized(appListeners) {
            for (CodewindApplication application : apps) {
                Set<AppKey> appKeys = appListeners.keySet();
                for (AppKey key : appKeys) {
                    if (application.connection.getConid().equals(key.connectionId) &&
                            application.projectID.equals(key.projectId)) {
                        AppUpdateListener listener = appListeners.get(key);
                        if (listener != null) {
                            listener.remove();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void removeApplication(CodewindApplication application) {
        synchronized(appListeners) {
            Set<AppKey> appKeys = appListeners.keySet();
            for (AppKey key : appKeys) {
                if (application.connection.getConid().equals(key.connectionId) &&
                        application.projectID.equals(key.projectId)) {
                    AppUpdateListener listener = appListeners.get(key);
                    if (listener != null) {
                        listener.remove();
                    }
                }
            }
        }
    }

    /**
     * Any Tool Window Content can be added as a listener for any application updates
     * To identify the correct window, the following arguments are required to differentiate
     *
     * @param content
     * @param connectionId
     * @param projectID
     * @param listener
     */
    public void addAppUpdateListener(Content content, String connectionId, String projectID, AppUpdateListener listener) {
        synchronized(appListeners) {
            appListeners.put(new AppKey(content, connectionId, projectID), listener);
        }
    }

    public void removeAppUpdateListener(Content content, String connectionId, String projectID) {
        synchronized(appListeners) {
            Set<AppKey> appKeys = appListeners.keySet();
            List<AppKey> keysToRemove = new ArrayList<>();
            for (AppKey key : appKeys) {
                AppUpdateListener listener = appListeners.get(key);
                if (content == key.content && key.connectionId.equals(connectionId) && key.projectId.equals(projectID)) {
                    if (listener != null) {
                        listener.remove();
                        keysToRemove.add(key);
                    }
                }
            }
            for (AppKey key : keysToRemove) {
                appListeners.remove(key);
            }
        }
    }

    public interface AppUpdateListener {
        public void update();
        public void remove();
    }

    private static class AppKey {
        public final String connectionId;
        public final String projectId;
        public final Content content;

        public AppKey(Content content, String connectionId, String projectID) {
            this.connectionId = connectionId;
            this.projectId = projectID;
            this.content = content;
        }
    }
}
