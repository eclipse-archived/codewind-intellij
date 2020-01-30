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

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RemoveProjectTask extends Task.Backgroundable {
    private final CodewindApplication application;

    public RemoveProjectTask(CodewindApplication application) {
        super(null, message("UnbindActionJobTitle"));
        this.application = application;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        try {
            ProjectUtil.removeProject(application.name, application.projectID, indicator);
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
        Logger.logWarning("An error occurred removing project " + application.name, t);
        Messages.showErrorDialog(message("UnbindActionError", application.name, t.getLocalizedMessage()), message("CodewindLabel"));
    }
}
