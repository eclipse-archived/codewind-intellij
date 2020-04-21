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
import org.jetbrains.annotations.NotNull;

public class EnableDisableInjectMetricsTask extends Task.Backgroundable {

    private final CodewindApplication application;

    public EnableDisableInjectMetricsTask(CodewindApplication application, Project project, String taskName) {
        super(project, taskName);
        this.application = application;
    }
    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        try {
            application.connection.requestInjectMetrics(application.projectID, !application.isMetricsInjected());
            application.connection.refreshApps(application.projectID);
        } catch (Exception e) {
            Logger.logWarning("An error occurred changing inject metric setting for: " + application.name + ", with id: " + application.projectID, e);

        }
    }
}
