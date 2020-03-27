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
package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.constants.StartMode;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RestartDebugModeTask extends Task.Backgroundable {
    private final CodewindApplication application;

    public RestartDebugModeTask(CodewindApplication application, Project project) {
        super(project, message("RestartInDebugMode"));
        this.application = application;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        if (application.projectLanguage.isJava()) {
            try {
                application.clearDebugger();
                // Restart the project in debug mode. The debugger will be attached when the restart result event is received from Codewind.
                // Try debug mode first since it allows debug of initialization.  If not supported use debugNoInit mode.
                if (application.getProjectCapabilities().supportsDebugMode()) {
                    application.connection.requestProjectRestart(application, StartMode.DEBUG.startMode);
                } else if (application.getProjectCapabilities().supportsDebugNoInitMode()) {
                    application.connection.requestProjectRestart(application, StartMode.DEBUG_NO_INIT.startMode);
                } else {
                    // Should never get here
                    Logger.log("Project restart in debug mode requested but project does not support any debug modes: " + application.name);
                }
            } catch (Exception e) {
                Logger.log(e);
            }
        }
    }
}