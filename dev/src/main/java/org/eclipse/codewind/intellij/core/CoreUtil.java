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

package org.eclipse.codewind.intellij.core;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.eclipse.codewind.intellij.ui.tree.CodewindToolWindowHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.intellij.openapi.application.ModalityState.defaultModalityState;
import static org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message;

/**
 * General utils that don't belong anywhere else
 */
public class CoreUtil {

    // Provide a way for users to override the path used for running commands
    private static final String ENV_PATH_PROPERTY = "org.eclipse.codewind.envPath";

    private static IUpdateHandler updateHandler;
    private static IUpdateHandler toolWindowUpdateHandler;
    private static NotificationGroup logUpdatesNotificationGroup;

	public enum DialogType {
		ERROR,
		WARN,
		INFO;
	};

	public static void initLogUpdatesNotificationGroup() {
	    if (logUpdatesNotificationGroup == null) {
            logUpdatesNotificationGroup = new NotificationGroup(message("LogUpdates"), NotificationDisplayType.TOOL_WINDOW, false, CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID, IconCache.getCachedIcon(IconCache.ICONS_THEMELESS_CODEWIND_SVG));
        }
    }

    public static NotificationGroup getLogUpdatesNotification() {
	    return logUpdatesNotificationGroup;
    }

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

    /**
     * Must use with invokeLater
     * @param title
     * @param msg
     * @return
     */
    public static int showYesNoDialog(String title, String msg) {
        return Messages.showYesNoDialog(msg, title, Messages.getQuestionIcon());
    }

    public static void openDialogWithLink(CoreUtil.DialogType type, String title, String msg, String linkLabel, String linkUrl) {
        invokeLater(new Runnable() {
            @Override
            public void run() {
                int messageType = JOptionPane.PLAIN_MESSAGE;
                switch (type) {
                    case ERROR: messageType = JOptionPane.ERROR_MESSAGE;
                        break;
                    case WARN: messageType = JOptionPane.WARNING_MESSAGE;
                        break;
                    case INFO: messageType = JOptionPane.INFORMATION_MESSAGE;
                        break;
                    default: messageType = JOptionPane.PLAIN_MESSAGE;
                }
                JOptionPane dialog = new JOptionPane(title, messageType, JOptionPane.OK_OPTION) {
                //MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), title, null, msg, type.getValue(), 0, IDialogConstants.OK_LABEL) {
                    @Override
                    //protected Control createCustomArea(Composite parent) {
                    public JInternalFrame createInternalFrame(Component parentComponent, String title) {
                        JInternalFrame retVal = super.createInternalFrame(parentComponent, title);
                        JLabel link = new JLabel("<a>" + linkLabel + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
                        add(link);
                        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        link.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent event) {
                                if (linkUrl != null  && linkUrl.length() > 0) {
                                    try {
                                        final URI uri = new URI(linkUrl);
                                        com.intellij.ide.browsers.BrowserLauncher.getInstance().browse(uri);
                                    } catch (URISyntaxException use) {
                                        Logger.log("An error occurred trying to open an external browser at: " + linkUrl);
                                        System.out.println("*** An error occurred trying to open an external browser at: " + linkUrl);
                                    }
                                }
                            }
                        });
                        return retVal;
                    }
                };
                dialog.setVisible(true);
            }
        });
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

    /**
     * In: [ "Here", "Is", "Some Input" ]
     * Separator: ", "
     * Out: "Here, Is, Some Input"
     */
    public static String formatString(String[] strArray, String separator) {
        return Arrays.stream(strArray).collect(Collectors.joining(separator));
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

    public static String getExecutablePath(String name) {
        String path = getEnvPath();
        if (path != null) {
            File file = new File(path, name);
            if (file.isFile() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        for (String dir : System.getenv("PATH").split(File.pathSeparator)) {
            File file = new File(dir, name);
            if (file.isFile() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
}
