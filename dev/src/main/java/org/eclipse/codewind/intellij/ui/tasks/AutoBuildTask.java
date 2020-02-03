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
import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AutoBuildTask extends Task.Backgroundable {

    private final CodewindApplication application;
    private final boolean enable;

    public AutoBuildTask(CodewindApplication application, boolean enable) {
        super(null, message("EnableDisableAutoBuildJob", application.name));
        this.application = application;
        this.enable = enable;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        try {
            String actionKey = enable ? CoreConstants.VALUE_ACTION_ENABLEAUTOBUILD : CoreConstants.VALUE_ACTION_DISABLEAUTOBUILD;
            application.connection.requestProjectBuild(application, actionKey);
            application.setAutoBuild(enable);
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
        Logger.logWarning("An error occurred changing auto build setting for: " + application.name + ", with id: " + application.projectID, t);
        Messages.showErrorDialog(message("ErrorOnEnableDisableAutoBuild", application.name, t.getLocalizedMessage()), "Codewind");
    }

    public static AutoBuildTask createEnabler(CodewindApplication application) {
        return new AutoBuildTask(application, true);
    }

    public static AutoBuildTask createDisabler(CodewindApplication application) {
        return new AutoBuildTask(application, false);
    }
}
