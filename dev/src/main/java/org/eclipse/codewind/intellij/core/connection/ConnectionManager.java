/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// import org.eclipse.codewind.core.internal.CodewindObjectFactory;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;

/**
 * Singleton class to keep track of the list of current Codewind connections,
 * and manage persisting them.
 */
public class ConnectionManager {

    // Singleton instance. Never access this directly. Use the instance() method.
    private static ConnectionManager instance;

    private final LocalConnection localConnection;
    private List<CodewindConnection> connections = new ArrayList<>();

    private ConnectionManager() {
        localConnection = CodewindConnection.createLocalConnection();
        add(localConnection);
        localConnection.refreshInstallStatus();
        if (localConnection.getInstallStatus().isStarted()) {
            CoreUtil.runAsync(() -> {
                try {
                    localConnection.connect();
                } catch (Exception e) {
                    Logger.logError("An error occurred trying to connect to the local Codewind instance at:" + localConnection.getBaseUri(), e); //$NON-NLS-1$
                }
            });
        }
    }

    public static synchronized ConnectionManager getManager() {
    	if (instance == null) {
 			instance = new ConnectionManager();
    	}
        return instance;
    }

    public LocalConnection getLocalConnection() {
        return localConnection;
    }

    /**
     * Adds the given connection to the list of connections.
     */
    public synchronized void add(CodewindConnection connection) {
        if (connection == null) {
            Logger.logError("Null connection passed to be added"); //$NON-NLS-1$
            return;
        }

        connections.add(connection);
        Logger.log("Added a new connection: " + connection.getBaseUri()); //$NON-NLS-1$
    }

    /**
     * @return An <b>unmodifiable</b> copy of the list of existing MC connections.
     */
    public synchronized List<CodewindConnection> activeConnections() {
        return Collections.unmodifiableList(connections);
    }

    public synchronized CodewindConnection getActiveConnection(String baseUrl) {
        for (CodewindConnection mcc : activeConnections()) {
            if (mcc.getBaseUri().toString().equals(baseUrl)) {
                return mcc;
            }
        }
        return null;
    }

    // Preferences serialization
    private void writeToPreferences() {
        // TODO: to be implemented
//		StringBuilder prefsBuilder = new StringBuilder();
//
//		for (CodewindConnection mcc : activeConnections()) {
//			prefsBuilder.append(mcc.toPrefsString()).append('\n');
//		}
//		for (String mcc : brokenConnections()) {
//			prefsBuilder.append(mcc).append('\n');
//		}
//
//		Logger.log("Writing connections to preferences: " + prefsBuilder.toString()); //$NON-NLS-1$
//
//		CodewindCorePlugin.getDefault().getPreferenceStore()
//				.setValue(CONNECTION_LIST_PREFSKEY, prefsBuilder.toString());
    }

    private void loadFromPreferences() {
        // TODO: to be implemented
//		clear();
//
//		String storedConnections = CodewindCorePlugin.getDefault()
//				.getPreferenceStore()
//				.getString(CONNECTION_LIST_PREFSKEY).trim();
//
//		Logger.log("Reading connections from preferences: \"" + storedConnections + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//		for(String line : storedConnections.split("\n")) { //$NON-NLS-1$
//			line = line.trim();
//			if(line.isEmpty()) {
//				continue;
//			}
//
//			try {
//				// Assume all connections are active. If they are broken they will be handled in the catch below.
//				URI uri = new URI(line);
//				CodewindConnection connection = CodewindObjectFactory.createCodewindConnection(uri);
//				add(connection);
//			}
//			catch (CodewindConnectionException mce) {
//				// The MC instance we wanted to connect to is down.
//				brokenConnections.add(mce.connectionUrl.toString());
//				CodewindReconnectJob.createAndStart(mce.connectionUrl);
//			}
//			catch (Exception e) {
//				Logger.logError("Error loading connection from preferences", e); //$NON-NLS-1$
//			}
//		}

    }
}
