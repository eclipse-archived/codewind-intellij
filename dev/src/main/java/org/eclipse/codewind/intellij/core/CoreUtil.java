/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static com.intellij.openapi.application.ModalityState.defaultModalityState;

/**
 * General utils that don't belong anywhere else
 */
public class CoreUtil {

    // Provide a way for users to override the path used for running commands
    private static final String ENV_PATH_PROPERTY = "org.eclipse.codewind.envPath";

    private static IUpdateHandler updateHandler;
    private static IUpdateHandler toolWindowUpdateHandler;

	public enum DialogType {
		ERROR,
		WARN,
		INFO;
	};


    /**
     * Open a dialog on top of the current active window. Can be called off the UI thread.
     */
    public static void openDialog(boolean isError, String title, String msg) {
		openDialog(isError ? DialogType.ERROR : DialogType.INFO, title, msg);
	}
	
	public static void openDialog(DialogType type, String title, String msg) {
        switch (type) {
            case ERROR:
                invokeLater(() -> Messages.showErrorDialog(msg, title));
                break;
            case WARN:
                invokeLater(() -> Messages.showWarningDialog(msg, title));
                break;
            case INFO:
                invokeLater(() -> Messages.showInfoMessage(msg, title));
                break;
        }
	}

    public static String readAllFromStream(InputStream stream) {
        Scanner s = new Scanner(stream);
        // end-of-stream
        s.useDelimiter("\\A"); //$NON-NLS-1$
        String result = s.hasNext() ? s.next() : ""; //$NON-NLS-1$
        s.close();
        return result;
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        return os != null && os.toLowerCase().startsWith("windows"); //$NON-NLS-1$
    }

    public static String getHostPath(String containerPath) {
        String hostPath = containerPath;
        if (isWindows() && containerPath.startsWith("/")) { //$NON-NLS-1$
            String device = containerPath.substring(1, 2);
            hostPath = device + ":" + containerPath.substring(2); //$NON-NLS-1$
        }
        return hostPath;
    }

    public static String getContainerPath(String hostPath) {
        String containerPath = hostPath;
        if (isWindows() && hostPath.indexOf(':') == 1) { //$NON-NLS-1$
            containerPath = "/" + hostPath.charAt(0) + hostPath.substring(2);
            containerPath = containerPath.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return containerPath;
    }

    /**
     * Append finish to start, removing the last segment of start if it is equal to the first segment of finish.
     */
    public static Path appendPathWithoutDupe(Path start, String finish) {
        if (finish.charAt(0) == File.separatorChar || finish.charAt(0) == '/')
            finish = finish.substring(1);
        Path finishPath = Paths.get(finish);
        if (start.endsWith(finishPath.getName(0))) {
            start = start.getParent();
        }
        return start.resolve(finishPath);
    }

    public static int parsePort(String portStr) {
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Logger.logWarning(String.format("Couldn't parse port from \"%s\"", portStr), e); //$NON-NLS-1$
            return -1;
        }
    }

    /**
     * Asynchronously execute the given runnable on the AWT event dispatch thread
     * using the given state.
     *
     * @param runner the runnable to execute.
     * @param state  the state in which the runnable will be executed.
     * @see ModalityState
     */
    public static void invokeLater(Runnable runner, ModalityState state) {
        ApplicationManager.getApplication().invokeLater(runner, state);
    }

    /**
     * Asynchronously execute the given runnable on the AWT event dispatch thread
     * using the default modality state.
     *
     * @param runner the runnable to execute.
     * @see ModalityState#defaultModalityState()
     */
    public static void invokeLater(Runnable runner) {
        invokeLater(runner, defaultModalityState());
    }

    /**
     * Asynchronously execute the given runnable on a background thread
     *
     * @param runner the runner to run
     */
    public static void runAsync(Runnable runner) {
        CompletableFuture.runAsync(runner);
    }

    /**
     * Update everything in the Codewind explorer view
     * Note: the update may be asynchronous
     */
    public static void updateAll() {
        IUpdateHandler handler = getUpdateHandler();
        IUpdateHandler toolWindowHandler = getToolWindowUpdateHandler();
        if (handler != null) {
            invokeLater(handler::updateAll);
        }
        if (toolWindowHandler != null) {
            invokeLater(toolWindowHandler::updateAll);
        }
    }

    /**
     * Update the connection and its children in the Codewind explorer view
     * Note: the update may be asynchronous
     */
    public static void updateConnection(CodewindConnection connection) {
        IUpdateHandler handler = getUpdateHandler();
        IUpdateHandler toolWindowHandler = getToolWindowUpdateHandler();
        if (handler != null) {
            invokeLater(() -> handler.updateConnection(connection));
        }
        if (toolWindowHandler != null) {
            invokeLater(() -> toolWindowHandler.updateConnection(connection));
        }
    }

    /**
     * Remove the connection.  Apps must be passed in since they may not
     * be available if the connection is no longer active.
     * Note: the update may be asynchronous
     */
    public static void removeConnection(List<CodewindApplication> apps) {
        IUpdateHandler handler = getUpdateHandler();
        IUpdateHandler toolWindowHandler = getToolWindowUpdateHandler();
        if (handler != null) {
            invokeLater(() -> handler.removeConnection(apps));
        }
        if (toolWindowHandler != null) {
            invokeLater(() -> toolWindowHandler.removeConnection(apps));
        }
    }

    /**
     * Update the application in the Codewind explorer view
     * Note: the update may be asynchronous
     */
    public static void updateApplication(CodewindApplication app) {
        IUpdateHandler handler = getUpdateHandler();
        IUpdateHandler handlers = getToolWindowUpdateHandler();
        if (handler != null) {
            invokeLater(() -> handler.updateApplication(app));
        }
        if (handlers != null) {
            invokeLater(() -> handlers.updateApplication(app));
        }
    }

    /**
     * Remove the application in the Codewind explorer view
     * Note: the update may be asynchronous
     */
    public static void removeApplication(CodewindApplication app) {
        IUpdateHandler handler = getUpdateHandler();
        IUpdateHandler toolWindowHandler = getToolWindowUpdateHandler();
        if (handler != null) {
            invokeLater(() -> handler.removeApplication(app));
        }
        if (toolWindowHandler != null) {
            invokeLater(() -> toolWindowHandler.removeApplication(app));
        }
    }

    public static String getOSName() {
        return (String) System.getProperty("os.name");
    }

    public static boolean isMACOS() {
        String osName = getOSName();
        if (osName != null && osName.toLowerCase().contains("mac")) {
            return true;
        }
        return false;
    }

    public static String getEnvPath() {
        String path = (String) System.getProperty(ENV_PATH_PROPERTY);
        if (path == null || path.trim().isEmpty()) {
            if (isMACOS()) {
                // On MAC a full path is required for running commands
                return "/usr/local/bin/";
            }
            return null;
        }
        path = path.trim();
        path = path.replace("\\", "/");
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    public static void setUpdateHandler(IUpdateHandler handler) {
        updateHandler = handler;
    }

    public static void setToolWindowUpdateHandler(IUpdateHandler handler) {
        toolWindowUpdateHandler = handler;
    }

    public static IUpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public static IUpdateHandler getToolWindowUpdateHandler() {
        return toolWindowUpdateHandler;
    }

}
