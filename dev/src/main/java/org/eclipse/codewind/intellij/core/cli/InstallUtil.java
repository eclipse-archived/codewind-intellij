/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *	 IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core.cli;

import com.intellij.openapi.progress.ProgressIndicator;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.eclipse.codewind.intellij.core.ProcessHelper.ProcessResult;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class InstallUtil {

    public static final String STOP_APP_CONTAINERS_PREFSKEY = "stopAppContainers";
    public static final String STOP_APP_CONTAINERS_ALWAYS = "stopAppContainersAlways";
    public static final String STOP_APP_CONTAINERS_NEVER = "stopAppContainersNever";
    public static final String STOP_APP_CONTAINERS_PROMPT = "stopAppContainersPrompt";
    public static final String STOP_APP_CONTAINERS_DEFAULT = STOP_APP_CONTAINERS_PROMPT;

    public static final int INSTALL_TIMEOUT_DEFAULT = 300;
    public static final int UNINSTALL_TIMEOUT_DEFAULT = 60;
    public static final int START_TIMEOUT_DEFAULT = 60;
    public static final int STOP_TIMEOUT_DEFAULT = 300;

	private static final String[] INSTALL_CMD = new String[] {"install"};
	private static final String[] START_CMD = new String[] {"start"};
	private static final String[] STOP_CMD = new String[] {"stop"};
	private static final String[] STOP_ALL_CMD = new String[] {"stop-all"};
	private static final String[] STATUS_CMD = new String[] {"status"};
	private static final String[] REMOVE_CMD = new String[] {"remove"};
	private static final String[] UPGRADE_CMD = new String[] {"upgrade"};

    private static final String INSTALL_VERSION_PROPERTIES = "install-version.properties";
    private static final String INSTALL_VERSION_KEY = "install-version";
    private static final String INSTALL_VERSION;
    private static final String TAG_OPTION = "-t";

    static {
        String version;
        try (InputStream stream = InstallUtil.class.getClassLoader().getResourceAsStream(INSTALL_VERSION_PROPERTIES)) {
            Properties properties = new Properties();
            properties.load(stream);
            version = properties.getProperty(INSTALL_VERSION_KEY);
        } catch (Exception e) {
            Logger.logWarning("Reading version from \"" + INSTALL_VERSION_PROPERTIES + " file failed, defaulting to \"latest\": ", e);
            version = CoreConstants.VERSION_LATEST;
        }
        INSTALL_VERSION = version;
    }

    public static InstallStatus getInstallStatus() throws IOException, JSONException, TimeoutException {
        ProcessResult result = statusCodewind();
        if (result.getExitValue() != 0) {
            String error = result.getError();
            if (error == null || error.isEmpty()) {
                error = result.getOutput();
            }
            String msg = "Installer status command failed with rc: " + result.getExitValue() + " and error: " + error;  //$NON-NLS-1$ //$NON-NLS-2$
            Logger.logWarning(msg);
            throw new IOException(msg);
        }
        JSONObject status = new JSONObject(result.getOutput());
        return new InstallStatus(status);
    }

    public static ProcessResult startCodewind(String version, ProgressIndicator indicator) throws IOException, TimeoutException, JSONException {
        indicator.setIndeterminate(true);
        Process process = null;
        try {
            ConnectionManager.getManager().getLocalConnection().setInstallerStatus(LocalConnection.InstallerStatus.STARTING);
            process = CLIUtil.runCWCTL(null, START_CMD, new String[] {TAG_OPTION, version});
            ProcessResult result = ProcessHelper.waitForProcess(process, 500, 120);
            return result;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            ConnectionManager.getManager().getLocalConnection().refreshInstallStatus();
            ConnectionManager.getManager().getLocalConnection().setInstallerStatus(null);
        }
    }

    public static ProcessResult stopCodewind(ProgressIndicator indicator) throws IOException, TimeoutException {
        indicator.setIndeterminate(true);

        // Disconnect the local connection, then yield to give it the chance to close.
        // If there's an exception, log it and continue to stop Codewind
        try {
            ConnectionManager.getManager().getLocalConnection().disconnect();
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // ignore
            }
        } catch (Exception e) {
            Logger.logWarning("Error closing socket", e);
        }

        Process process = null;
        try {
            ConnectionManager.getManager().getLocalConnection().setInstallerStatus(LocalConnection.InstallerStatus.STOPPING);
            process = CLIUtil.runCWCTL(null, STOP_ALL_CMD, null);
            return ProcessHelper.waitForProcess(process, 500, 120);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            ConnectionManager.getManager().getLocalConnection().refreshInstallStatus();
            ConnectionManager.getManager().getLocalConnection().setInstallerStatus(null);
        }
    }

    private static ProcessResult statusCodewind() throws IOException, TimeoutException {
        Process process = null;
        try {
            process = CLIUtil.runCWCTL(CLIUtil.GLOBAL_JSON, STATUS_CMD, null);
            ProcessResult result = ProcessHelper.waitForProcess(process, 500, 120);
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            if (t instanceof IOException)
                throw (IOException) t;
            if (t instanceof TimeoutException)
                throw (TimeoutException) t;
            throw new RuntimeException(t);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    public static String getVersion() {
        return INSTALL_VERSION;
    }
}
