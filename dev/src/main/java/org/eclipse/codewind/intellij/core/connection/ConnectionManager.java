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

import okhttp3.*;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.log4j.Level;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CodewindManager;
import org.eclipse.codewind.intellij.core.CodewindObjectFactory;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.AuthToken;
import org.eclipse.codewind.intellij.core.cli.AuthUtil;
import org.eclipse.codewind.intellij.core.cli.ConnectionInfo;
import org.eclipse.codewind.intellij.core.cli.ConnectionUtil;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
//		System.out.println("Setting okHttp logger");
//		java.util.logging.Logger.getLogger(OkHttpClient.class.getName()).setLevel(java.util.logging.Level.FINE);
//		java.util.logging.Logger.getLogger(OkHttpClient.class.getName()).fine("******* CODEWIND LOG");
//		try {
//			java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OkHttpClient.class.getName());
//			System.out.println(logger.getClass().getClassLoader().getResource("logging.properties"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("OKs " + OkHttpClient.class.getResource("logging.properties"));
		CoreUtil.runAsync( () -> {
			try {
				restoreConnections();
//				add(localConnection);
				// This will connect if Codewind is running
				CodewindManager.getManager().refreshInstallStatus();
				CoreUtil.updateAll();
			} catch (Exception e) {
				e.printStackTrace();
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
        return getManager().localConnection;
    }

    private void restoreConnections() {

		ProgressIndicator mon = new EmptyProgressIndicator();
		add(localConnection);

		// Add the rest of the connections, skipping local
		try {
			List<ConnectionInfo> infos = ConnectionUtil.listConnections(mon);
			int workRemaining = 100 * (infos.size() -1);
			if (infos.size() > 1) {
				mon.setFraction(1 / workRemaining);
			}
			for (ConnectionInfo info : infos) {
				try {
					if (!info.isLocal()) {
						URI uri = new URI(info.getURL());
						AuthToken auth = null;
						try {
							auth = AuthUtil.getAuthToken(info.getUsername(), info.getId(), mon);
						} catch (Exception e) {
							Logger.logWarning("An error occurred trying to get the authorization token for: " + info.getId(), e); //$NON-NLS-1$
						}
						if (mon.isCanceled()) {
							return;
						}
						CodewindConnection conn = CodewindObjectFactory.createRemoteConnection(info.getLabel(), uri, info.getId(), info.getUsername(), auth);
						connections.add(conn);
						if (auth != null) {
							conn.connect();
						}
						if (mon.isCanceled()) {
							return;
						}
					}
				} catch (Exception e) {
					Logger.logWarning("Error restoring connections", e);
				} finally {
					CoreUtil.updateAll();
				}
			}
			return;
		} catch (Exception e) {
			Logger.logWarning("An error occurred trying to restore the connections", e); //$NON-NLS-1$
		}

	}

    /**
     * Adds the given connection to the list of connections.
     */
    public synchronized static void add(CodewindConnection connection) {
        if (connection == null) {
            Logger.logWarning("Null connection passed to be added"); //$NON-NLS-1$
            return;
        }
        System.out.println("****** connection is connected? " + connection.isConnected());
        getManager().connections.add(connection);
        Logger.log("Added a new connection: " + connection.getBaseURI()); //$NON-NLS-1$
    }

    /**
     * @return An <b>unmodifiable</b> copy of the list of existing MC connections.
     */
    public synchronized static List<CodewindConnection> activeConnections() {
        return Collections.unmodifiableList(getManager().connections);
    }

    public synchronized static CodewindConnection getActiveConnection(String baseUrl) {
        for (CodewindConnection mcc : activeConnections()) {
            if (mcc.getBaseURI() != null && mcc.getBaseURI().toString().equals(baseUrl)) {
                return mcc;
            }
        }
        return null;
    }

	public synchronized static List<CodewindConnection> activeRemoteConnections() {
		return activeConnections().stream().filter(conn -> !conn.isLocal()).collect(Collectors.toList());
	}

	public synchronized static CodewindConnection getConnectionById(String id) {
		for (CodewindConnection conn : activeConnections()) {
			if (conn.getBaseURI() != null && conn.getConid().equals(id)) {
				return conn;
			}
		}
		return null;
	}

	public synchronized static CodewindConnection getActiveConnectionByName(String name) {
		for(CodewindConnection conn : activeConnections()) {
			if(name != null && name.equals(conn.getName())) {
				return conn;
    }
		}
		return null;
	}

	public synchronized int activeConnectionsCount() {
		return getManager().connections.size();
	}

	/**
	 * Try to remove the given connection.
	 * @return
	 * 	true if the connection was removed,
	 * 	false if not because it didn't exist.
	 */
	public synchronized static boolean remove(String baseUrl) {
		boolean removeResult = false;

		CodewindConnection connection = getActiveConnection(baseUrl.toString());
		if (connection != null) {
			List<CodewindApplication> apps = connection.getApps();
			connection.close();
			removeResult = getManager().connections.remove(connection);
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
		Logger.log("Clearing " + getManager().connections.size() + " connections"); //$NON-NLS-1$ //$NON-NLS-2$

		Iterator<CodewindConnection> it = getManager().connections.iterator();

		while(it.hasNext()) {
			CodewindConnection connection = it.next();
			connection.close();
			it.remove();
		}
	}
}
