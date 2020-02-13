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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;
import org.eclipse.codewind.intellij.core.*;
import org.eclipse.codewind.intellij.core.cli.AuthToken;
import org.eclipse.codewind.intellij.core.console.ProjectLogInfo;
import org.eclipse.codewind.intellij.core.console.SocketConsole;
import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.eclipse.codewind.intellij.core.constants.ProjectType;
import org.eclipse.codewind.intellij.core.constants.StartMode;
import org.jetbrains.annotations.NonNls;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message;

/**
 * Wrapper for a SocketIO client socket, which connects to Codewind and listens for project state changes,
 * then updates the corresponding CodewindApplication's state.
 * One of these exists for each CodewindConnection. That connection is stored here so we can access
 * its applications.
 */
public class CodewindSocket {

    private final CodewindConnection connection;

    public final Socket socket;

    public final URI socketUri;

    private boolean hasLostConnection = false;

    private volatile boolean hasConnected = false;

     private Set<SocketConsole> socketConsoles = new HashSet<>();

    // Track the previous Exception so we don't spam the logs with the same connection failure message
    private Throwable previousException;

    // SocketIO Event names
    @NonNls
    private static final String
            EVENT_PROJECT_CREATION = "projectCreation",
            EVENT_PROJECT_CHANGED = "projectChanged",
            EVENT_PROJECT_STATUS_CHANGE = "projectStatusChanged",
            EVENT_PROJECT_RESTART = "projectRestartResult",
            EVENT_PROJECT_CLOSED = "projectClosed",
            EVENT_PROJECT_DELETION = "projectDeletion",
            EVENT_PROJECT_VALIDATED = "projectValidated",
            EVENT_LOG_UPDATE = "log-update",
            EVENT_PROJECT_LOGS_LIST_CHANGED = "projectLogsListChanged",
            EVENT_PROJECT_SETTINGS_CHANGED = "projectSettingsChanged";

	public CodewindSocket(CodewindConnection connection, AuthToken authToken) {
        this.connection = connection;

        URI uri = connection.getBaseURI();
        if (connection.getSocketNamespace() != null) {
            uri = uri.resolve(connection.getSocketNamespace());
        }
        socketUri = uri;

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		if (authToken != null) {
			builder
				.hostnameVerifier(HttpUtil.hostnameVerifier)
				.sslSocketFactory(HttpUtil.sslContext.getSocketFactory(), HttpUtil.trustManager);
		}
		OkHttpClient okHttpClient = builder
				.readTimeout(0L, TimeUnit.MILLISECONDS)
				.build();
			IO.setDefaultOkHttpCallFactory(okHttpClient);
			IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
			IO.Options opts = new IO.Options();
			opts.callFactory = okHttpClient;
			opts.webSocketFactory = okHttpClient;
			socket = IO.socket(socketUri, opts);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
				if (authToken != null) {
					try {
						JSONObject obj = new JSONObject();
						obj.put("token", authToken.getToken());
						socket.emit("authentication", obj.toString());
					} catch (Exception e) {
						Logger.logWarning("An error occurred trying to pass the authentication token to the socket", e);
						return;
					}
				}
                if (!hasConnected) {
                    hasConnected = true;
                    Logger.log("SocketIO connect success @ " + socketUri); //$NON-NLS-1$
                }
                if (hasLostConnection) {
                    connection.clearConnectionError();
                    previousException = null;
                }
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                if (arg0[0] instanceof Exception) {
                    Throwable e = Logger.unwrap((Exception) arg0[0]);
                    if (previousException == null || !e.getMessage().equals(previousException.getMessage())) {
                        previousException = e;
                        Logger.logWarning("SocketIO Connect Error @ " + socketUri, e); //$NON-NLS-1$
                    }
                }
                connection.onConnectionError();
                hasLostConnection = true;
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                if (arg0[0] instanceof Exception) {
					Exception e = (Exception) arg0[0];
					Logger.logWarning("SocketIO Error @ " + socketUri, Logger.unwrap(e)); //$NON-NLS-1$
                }
            }
        }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                // Don't think this is ever used
                Logger.log("SocketIO EVENT_MESSAGE " + arg0[0].toString()); //$NON-NLS-1$
            }
        }).on(EVENT_PROJECT_CREATION, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_CREATION + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectCreation(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_CHANGED, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_CHANGED + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectChanged(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_SETTINGS_CHANGED, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_SETTINGS_CHANGED + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectSettingsChanged(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_STATUS_CHANGE, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_STATUS_CHANGE + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectStatusChanged(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_RESTART, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_RESTART + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectRestart(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_CLOSED, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_CLOSED + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectClosed(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_DELETION, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_DELETION + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectDeletion(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_LOGS_LIST_CHANGED, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_LOGS_LIST_CHANGED + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onProjectLogsListChanged(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_LOG_UPDATE, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                // can't print this whole thing because the logs strings flood the output
                Logger.log(EVENT_LOG_UPDATE);

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onLogUpdate(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        }).on(EVENT_PROJECT_VALIDATED, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                Logger.log(EVENT_PROJECT_VALIDATED + ": " + arg0[0].toString()); //$NON-NLS-1$

                try {
                    JSONObject event = new JSONObject(arg0[0].toString());
                    onValidationEvent(event);
                } catch (JSONException e) {
                    Logger.logWarning("Error parsing JSON: " + arg0[0].toString(), e); //$NON-NLS-1$
                }
            }
        });

        socket.connect();
        Logger.log("Created CodewindSocket connected to " + socketUri); //$NON-NLS-1$
    }

    public void close() {
        if (socket != null) {
            if (socket.connected()) {
                socket.disconnect();
            }
            socket.close();
        }
    }

    private void onProjectCreation(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        connection.refreshApps(projectID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app != null) {
            app.setEnabled(true);
            CoreUtil.updateApplication(app);
        } else {
            Logger.logWarning("No application found matching the project id for the project creation event: " + projectID); //$NON-NLS-1$
        }
    }

    private void onProjectChanged(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            Logger.logWarning("No application found matching the project id for the project changed event: " + projectID); //$NON-NLS-1$
            return;
        }

        CodewindApplicationFactory.updateApp(app, event);

        // Reconnect debugger if necessary
        if (StartMode.DEBUG_MODES.contains(app.getStartMode()) && app.getDebugPort() != -1) {
            app.reconnectDebugger();
        }

        CoreUtil.updateApplication(app);
    }

    private void onProjectSettingsChanged(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            Logger.logWarning("No application found matching the project id for the project settings changed event: " + projectID); //$NON-NLS-1$
            return;
        }

        app.setEnabled(true);

        // Check the status
        if (event.has(CoreConstants.KEY_STATUS)) {
            String status = event.getString(CoreConstants.KEY_STATUS);
            if (!CoreConstants.VALUE_STATUS_SUCCESS.equals(status)) {
                if (event.has(CoreConstants.KEY_ERROR)) {
                    String errorMsg = event.getString(CoreConstants.KEY_ERROR);
                    CoreUtil.openDialog(true, message("ProjectSettingsUpdateErrorTitle"), errorMsg);
                } else {
                    Logger.logWarning("The project settings request failed but there is no error message in the result");
                }
                return;
            }
        }

        // Update project
        if (event.has(CoreConstants.KEY_CONTEXT_ROOT)) {
            app.setContextRoot(event.getString(CoreConstants.KEY_CONTEXT_ROOT));
        }
        if (event.has(CoreConstants.KEY_PORTS) && (event.get(CoreConstants.KEY_PORTS) instanceof JSONObject)) {
            JSONObject portsObj = event.getJSONObject(CoreConstants.KEY_PORTS);
            if (portsObj.has(CoreConstants.KEY_INTERNAL_PORT)) {
                app.setContainerAppPort(portsObj.getString(CoreConstants.KEY_INTERNAL_PORT));
            }
            if (portsObj.has(CoreConstants.KEY_INTERNAL_DEBUG_PORT)) {
                app.setContainerDebugPort(portsObj.getString(CoreConstants.KEY_INTERNAL_DEBUG_PORT));
            }
        }


        CoreUtil.updateApplication(app);
    }

    private void onProjectStatusChanged(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            // Likely a new project is being created
            connection.refreshApps(projectID);
            CoreUtil.updateConnection(connection);
            return;
        }

        CodewindApplicationFactory.updateApp(app, event);
        CoreUtil.updateApplication(app);
    }

    private void onProjectRestart(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            Logger.logWarning("No application found matching the project id for the project restart event: " + projectID); //$NON-NLS-1$
            return;
        }

        app.setEnabled(true);

        String status = event.getString(CoreConstants.KEY_STATUS);
        if (!CoreConstants.REQUEST_STATUS_SUCCESS.equalsIgnoreCase(status)) {
            Logger.logWarning("Project restart failed on the application: " + event.toString()); //$NON-NLS-1$
            CoreUtil.openDialog(true,
                    message("Socket_ErrRestartingProjectDialogTitle"),
                    message("Socket_ErrRestartingProjectDialogMsg",
                            app.name, status));
            return;
        }

        // This event should always have a 'ports' sub-object
        JSONObject portsObj = event.getJSONObject(CoreConstants.KEY_PORTS);

        // The ports object should always have an http port
        if (portsObj != null && portsObj.has(CoreConstants.KEY_EXPOSED_PORT)) {
            int port = CoreUtil.parsePort(portsObj.getString(CoreConstants.KEY_EXPOSED_PORT));
            app.setHttpPort(port);
        } else {
            Logger.logWarning("No http port on project restart event for: " + app.name); //$NON-NLS-1$
        }

        // Debug port will be missing if the restart was into Run mode.
        int debugPort = -1;
        if (portsObj != null && portsObj.has(CoreConstants.KEY_EXPOSED_DEBUG_PORT)) {
            debugPort = CoreUtil.parsePort(portsObj.getString(CoreConstants.KEY_EXPOSED_DEBUG_PORT));
        }
        app.setDebugPort(debugPort);

        StartMode startMode = StartMode.get(event);
        app.setStartMode(startMode);

        if (event.has(CoreConstants.KEY_CONTAINER_ID)) {
            String containerId = event.getString(CoreConstants.KEY_CONTAINER_ID);
            app.setContainerId(containerId);
        }

        // Update the application
        CoreUtil.updateApplication(app);

        // Make sure no old debugger is running
        app.clearDebugger();

        if (StartMode.DEBUG_MODES.contains(startMode) && debugPort != -1) {
            app.connectDebugger();
        }
    }

    private void onProjectClosed(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            Logger.logWarning("No application found for project being closed: " + projectID); //$NON-NLS-1$
            return;
        }
        app.dispose();
        app.connection.refreshApps(app.projectID);
        CoreUtil.updateApplication(app);
    }

    private void onProjectDeletion(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            Logger.log("No application found for project being deleted: " + projectID);
            return;
        }
        connection.removeApp(projectID);
    }

    public void registerSocketConsole(SocketConsole console) {
        Logger.log("Register socketConsole for project: " + console.app.name); //$NON-NLS-1$
        this.socketConsoles.add(console);
    }

    public void deregisterSocketConsole(SocketConsole console) {
        this.socketConsoles.remove(console);
    }

    private void onLogUpdate(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        String type = event.getString(CoreConstants.KEY_LOG_TYPE);
        String logName = event.getString(CoreConstants.KEY_LOG_NAME);
        Logger.log("Update the " + logName + " log for project: " + projectID);

        for (SocketConsole console : this.socketConsoles) {
            if (console.app.projectID.equals(projectID) && console.logInfo.isThisLogInfo(type, logName)) {
                try {
                    String logContents = event.getString(CoreConstants.KEY_LOGS);
                    boolean reset = event.getBoolean(CoreConstants.KEY_LOG_RESET);
                    console.update(logContents, reset);
                } catch (IOException e) {
                    Logger.logWarning("Error updating console " + logName, e);
                }
            }
        }
    }

    private void onProjectLogsListChanged(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            // Likely a new project is being created
            connection.refreshApps(projectID);
            CoreUtil.updateConnection(connection);
            return;
        }

        String type;
        if (event.has(CoreConstants.KEY_LOG_BUILD)) {
            type = CoreConstants.KEY_LOG_BUILD;
            JSONArray logs = event.getJSONArray(CoreConstants.KEY_LOG_BUILD);
            List<ProjectLogInfo> logInfos = CodewindConnection.getLogs(logs, type);
            app.addLogInfos(logInfos);
        }
        if (event.has(CoreConstants.KEY_LOG_APP)) {
            type = CoreConstants.KEY_LOG_APP;
            JSONArray logs = event.getJSONArray(CoreConstants.KEY_LOG_APP);
            List<ProjectLogInfo> logInfos = CodewindConnection.getLogs(logs, type);
            app.addLogInfos(logInfos);
        }
    }

    private void onValidationEvent(JSONObject event) throws JSONException {
        String projectID = event.getString(CoreConstants.KEY_PROJECT_ID);
        CodewindApplication app = connection.getAppByID(projectID);
        if (app == null) {
            Logger.logWarning("No application found for project: " + projectID); //$NON-NLS-1$
            return;
        }

        // Clear out any old validation objects
        app.resetValidation();

        // If the validation is successful then just return
        String status = event.getString(CoreConstants.KEY_VALIDATION_STATUS);
        if (CoreConstants.VALUE_STATUS_SUCCESS.equals(status)) {
            // Nothing to do
            return;
        }

        // If the validation is not successful, create validation objects for each problem
        if (event.has(CoreConstants.KEY_VALIDATION_RESULTS)) {
            JSONArray results = event.getJSONArray(CoreConstants.KEY_VALIDATION_RESULTS);
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String severity = result.getString(CoreConstants.KEY_SEVERITY);
                String filename = result.getString(CoreConstants.KEY_FILENAME);
                String filepath = result.getString(CoreConstants.KEY_FILEPATH);
                String type = null;
                if (result.has(CoreConstants.KEY_TYPE)) {
                    type = result.getString(CoreConstants.KEY_TYPE);
                }
                String details = result.getString(CoreConstants.KEY_DETAILS);
                String quickFixId = null;
                String quickFixDescription = null;
                if (result.has(CoreConstants.KEY_QUICKFIX) && supportsQuickFix(app, type, filename)) {
                    JSONObject quickFix = result.getJSONObject(CoreConstants.KEY_QUICKFIX);
                    quickFixId = quickFix.getString(CoreConstants.KEY_FIXID);
                    quickFixDescription = quickFix.getString(CoreConstants.KEY_DESCRIPTION);
                }
                if (CoreConstants.VALUE_SEVERITY_WARNING.equals(severity)) {
                    app.validationWarning(filepath, details, quickFixId, quickFixDescription);
                } else {
                    app.validationError(filepath, details, quickFixId, quickFixDescription);
                }
            }
        } else {
            Logger.log("Validation event indicates failure but no validation results,"); //$NON-NLS-1$
        }
    }

    private boolean supportsQuickFix(CodewindApplication app, String type, String filename) {
        // The regenerate job only works in certain cases so only show the quickfix in the working cases
        if (!CoreConstants.VALUE_TYPE_MISSING.equals(type) || app.projectType == ProjectType.TYPE_DOCKER) {
            return false;
        }
        if (CoreConstants.DOCKERFILE.equals(filename)) {
            return true;
        }
        if (app.projectType == ProjectType.TYPE_LIBERTY && CoreConstants.DOCKERFILE_BUILD.equals(filename)) {
            return true;
        }
        return false;
    }

    boolean blockUntilFirstConnection() {
        final int delay = 100;
        final int timeout = 2500;
        int waited = 0;
        while (!hasConnected && waited < timeout) {
            try {
                Thread.sleep(delay);
                waited += delay;

                if (waited % (5 * delay) == 0) {
                    Logger.log("Waiting for CodewindSocket initial connection"); //$NON-NLS-1$
                }
            } catch (InterruptedException e) {
                Logger.logWarning(e);
            }
        }
        Logger.log("CodewindSocket initialized in time ? " + hasConnected); //$NON-NLS-1$
        return hasConnected;
    }
}
