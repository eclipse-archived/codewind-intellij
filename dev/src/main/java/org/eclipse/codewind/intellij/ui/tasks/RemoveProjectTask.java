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
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.ProjectUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RemoveProjectTask extends Task.Backgroundable {
    private final List<CodewindApplication> applications;
    private CodewindApplication errorApp;

    public RemoveProjectTask(List<CodewindApplication> applications) {
        super(null, message("UnbindActionJobTitle"));
        this.applications = applications;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        try {
            for (CodewindApplication application: applications) {
                errorApp = application;
                ProjectUtil.removeProject(application.name, application.projectID, indicator);
                errorApp = null;
            }
        } catch (Exception e) {
            // Rethrown exception will be handled in #onThrowable()
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        Throwable t = error;
        while (t.getCause() != null)
            t = t.getCause();
        Logger.logWarning("An error occurred removing project " + errorApp.name, t);
        Messages.showErrorDialog(message("UnbindActionError", errorApp.name, t.getLocalizedMessage()), message("CodewindLabel"));
    }
}
