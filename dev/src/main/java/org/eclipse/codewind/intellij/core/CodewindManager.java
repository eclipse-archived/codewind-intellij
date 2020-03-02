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

package org.eclipse.codewind.intellij.core;

import org.eclipse.codewind.intellij.core.cli.InstallStatus;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;

import java.net.URI;

public class CodewindManager {
	
	private static CodewindManager codewindManager;
	
	// Keep track of the install status and if the installer is currently running.
	// If the installer is running, this is the status that should be reported, if
	// not the install status should be reported (installerStatus will be null).
	InstallStatus installStatus = InstallStatus.UNKNOWN;
	InstallerStatus installerStatus = null;
	String installerErrorMsg = null;
	
	public enum InstallerStatus {
		INSTALLING,
		UNINSTALLING,
		STARTING,
		STOPPING
	};
	
	private CodewindManager() {
	}

	public static synchronized CodewindManager getManager() {
		if (codewindManager == null) {
			codewindManager = new CodewindManager();
		}
		return codewindManager;
	}
	
	/**
	 * Get the current install status for Codewind
	 */
	public InstallStatus getInstallStatus() {
		return installStatus;
	}
	
	public String getInstallerErrorMsg() {
		return installerErrorMsg;
	}

	public synchronized void refreshInstallStatus() {
		String urlStr = null;
		installerErrorMsg = null;
		try {
			installStatus = InstallUtil.getInstallStatus();
			CodewindConnection localConnection = ConnectionManager.getManager().getLocalConnection();
			if (localConnection != null) {
				if (installStatus.isStarted()) {
					urlStr = installStatus.getURL();
					if (!localConnection.isConnected()) {
						URI uri = new URI(urlStr);
						localConnection.setBaseURI(uri);
						localConnection.connect();
					}
				} else {
					localConnection.disconnect();
					localConnection.setBaseURI(null);
				}
			}
			return;
		} catch (Exception e) {
			Logger.log("An error occurred trying to get the Codewind install status.", e); //$NON-NLS-1$
			System.out.println("*** An error occurred trying to get the Codewind install status. " + e.getLocalizedMessage());
			installerErrorMsg = e.getLocalizedMessage();
		}
		installStatus = InstallStatus.ERROR;
	}
	
	public InstallerStatus getInstallerStatus() {
		return installerStatus;
	}
	
	public void setInstallerStatus(InstallerStatus status) {
		this.installerStatus = status;
		CoreUtil.updateAll();
	}

	public boolean isSupportedVersion(String version) {
		return CodewindConnection.isSupportedVersion(version);
	}
	
	public void refresh() {
		refreshInstallStatus();
		for (CodewindConnection conn : ConnectionManager.getManager().activeConnections()) {
			if (conn.isConnected()) {
				conn.refreshApps(null);
			}
		}
	}
	
	public boolean hasActiveApplications() {
		for (CodewindConnection conn : ConnectionManager.getManager().activeConnections()) {
			for (CodewindApplication app : conn.getApps()) {
				if (app.isAvailable()) {
					return true;
				}
			}
		}
		return false;
	}
}
