/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core.connection;

import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.cli.InstallStatus;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message;

public class LocalConnection extends CodewindConnection {

    // Keep track of the install status and if the installer is currently running.
    // If the installer is running, this is the status that should be reported, if
    // not the install status should be reported (installerStatus will be null).
    private InstallStatus installStatus = InstallStatus.UNKNOWN;
    private InstallerStatus installerStatus = null;

    public enum InstallerStatus {
        INSTALLING,
        UNINSTALLING,
        STARTING,
        STOPPING
    }

    private static final String CONNECTION_ID = "local";

    public LocalConnection(String name, URI uri) {
        super(name, uri, CONNECTION_ID, null);
    }

    /**
     * Get the current install status for the local connection
     */
    public InstallStatus getInstallStatus() {
        return installStatus;
    }

    public synchronized void refreshInstallStatus() {
        String url = null;
        try {
            installStatus = InstallUtil.getInstallStatus();
            if (!installStatus.isStarted()) {
                disconnect();
                setBaseUri(null);
            } else if (!isConnected()) {
                URI uri = new URI(installStatus.getURL());
                setBaseUri(uri);
                connect();
            }
            return;
        } catch (IOException e) {
            Logger.logWarning("An error occurred trying to get the installer status", e); //$NON-NLS-1$
        } catch (TimeoutException e) {
            Logger.logWarning("Timed out trying to get the installer status", e); //$NON-NLS-1$
        } catch (JSONException e) {
            Logger.logWarning("The Codewind installer status format is not recognized", e); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            Logger.logWarning("The Codewind installer status command returned an invalid url: " + url, e);
        }
        installStatus = InstallStatus.UNKNOWN;
    }

    public InstallerStatus getInstallerStatus() {
        return installerStatus;
    }

    public void setInstallerStatus(InstallerStatus status) {
        this.installerStatus = status;
        CoreUtil.updateAll();
    }

}
