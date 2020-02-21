/*******************************************************************************
 * Copyright (c) 2018, 2020 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

// import org.eclipse.codewind.core.internal.CodewindObjectFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.ConnectionInfo;
import org.eclipse.codewind.intellij.core.cli.ConnectionUtil;

/**
 * Singleton class to keep track of the list of current Codewind connections,
 * and manage persisting them.
 */
public class ConnectionManager {

    // Singleton instance. Never access this directly. Use the getManager() method.
    private static ConnectionManager instance;

    private final LocalConnection localConnection;
    private List<CodewindConnection> connections = new ArrayList<>();

    private ConnectionManager() {
		localConnection = CodewindConnection.createLocalConnection();
		add(localConnection);
		CoreUtil.runAsync( () -> {
			try {
				// This will connect if Codewind is running
				localConnection.refreshInstallStatus();
				CoreUtil.updateAll();
			} catch (Exception e) {
				Logger.logWarning("An error occurred trying to connect to the local Codewind instance at:" + localConnection.getBaseURI(), e); //$NON-NLS-1$
			}
		});
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
            Logger.logWarning("Null connection passed to be added"); //$NON-NLS-1$
            return;
        }

        connections.add(connection);
        Logger.log("Added a new connection: " + connection.getBaseURI()); //$NON-NLS-1$
    }

    /**
     * @return An <b>unmodifiable</b> copy of the list of existing MC connections.
     */
    public synchronized List<CodewindConnection> activeConnections() {
        return Collections.unmodifiableList(connections);
    }

    public synchronized CodewindConnection getActiveConnection(String baseUrl) {
        for (CodewindConnection mcc : activeConnections()) {
            if (mcc.getBaseURI().toString().equals(baseUrl)) {
                return mcc;
            }
        }
        return null;
    }

	public synchronized CodewindConnection getActiveConnectionByName(String name) {
		for(CodewindConnection conn : activeConnections()) {
			if(name != null && name.equals(conn.getName())) {
				return conn;
    }
		}
		return null;
	}

	public synchronized int activeConnectionsCount() {
		return connections.size();
	}

	/**
	 * Try to remove the given connection.
	 * @return
	 * 	true if the connection was removed,
	 * 	false if not because it didn't exist.
	 */
	public synchronized boolean remove(String baseUrl) {
		boolean removeResult = false;

		CodewindConnection connection = getActiveConnection(baseUrl.toString());
		if (connection != null) {
			List<CodewindApplication> apps = connection.getApps();
			connection.close();
			removeResult = connections.remove(connection);
			CoreUtil.removeConnection(apps);
			if (connection.getConid() != null) {
				try {
					ConnectionUtil.removeConnection(connection.getConid(), new EmptyProgressIndicator());
				} catch (Exception e) {
					Logger.logWarning("An error occurred trying to de-register the connection: " + connection.getName()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		if (!removeResult) {
			Logger.logWarning("Tried to remove connection " + baseUrl + ", but it didn't exist"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		CoreUtil.updateAll();
		return removeResult;
	}

	/**
	 * Deletes all of the instance's connections. Called when the plugin is stopped.
	 */
	public synchronized void clear() {
		Logger.log("Clearing " + connections.size() + " connections"); //$NON-NLS-1$ //$NON-NLS-2$

		Iterator<CodewindConnection> it = connections.iterator();

		while(it.hasNext()) {
			CodewindConnection connection = it.next();
			connection.close();
			it.remove();
		}
	}
}
