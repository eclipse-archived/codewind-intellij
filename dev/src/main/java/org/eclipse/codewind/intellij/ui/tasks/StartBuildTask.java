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

package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class StartBuildTask extends Task.Backgroundable {

    @NotNull
    private final CodewindApplication application;

    public StartBuildTask(CodewindApplication application) {
        super(null, "Build application " + application.getName());
        this.application = application;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {

        try {
            application.connection.requestProjectBuild(application, CoreConstants.VALUE_ACTION_BUILD);
        } catch (Exception e) {
            // Handled in #onThrowable()
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        Throwable t = error;
        while (t.getCause() != null)
            t = t.getCause();
        Logger.logWarning("An error occurred building project " + application.getName(), t);
        Messages.showErrorDialog(message("StartBuildError", application.getName(), t.getLocalizedMessage()), "Codewind");
    }
}
