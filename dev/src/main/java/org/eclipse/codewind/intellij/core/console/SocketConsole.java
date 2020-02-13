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
package org.eclipse.codewind.intellij.core.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ui.content.Content;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindSocket;

import java.io.IOException;

public class SocketConsole {
    public final CodewindApplication app;
    public final ProjectLogInfo logInfo;
    private final CodewindSocket socket;

    private boolean isInitialized = false;
    private Content content;
    private ConsoleView consoleView;

    public SocketConsole(Content content, ConsoleView consoleView, String consoleName, ProjectLogInfo logInfo, CodewindApplication app) {
        this.logInfo = logInfo;
        this.app = app;
        this.socket = app.connection.getSocket();
        this.content = content;
        this.consoleView = consoleView;
    }

    public void initialize() throws Exception {
        SocketConsole socketConsole = this;
        CoreUtil.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.registerSocketConsole(socketConsole);
                    app.connection.requestEnableLogStream(app, logInfo);
                } catch (Exception e) {

                }
            }
        });
    }

    public void update(String contents, boolean reset) throws IOException {
        if (!isInitialized || reset) {
            if (consoleView != null) {
                consoleView.clear();
            }
            isInitialized = true;
        }
        // Todo: investigate how else we can print with different content types
        consoleView.print(contents, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public void dispose() {
        Logger.log("Dispose console " + logInfo.logName);
        socket.deregisterSocketConsole(this);
        if (app.isAvailable()) {
            try {
                app.connection.requestDisableLogStream(app, logInfo);
            } catch (Exception e) {
                Logger.logWarning("Error disabling the log stream for: " + app.name, e);
            }
        }
    }
}