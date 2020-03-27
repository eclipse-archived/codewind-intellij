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
package org.eclipse.codewind.intellij.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.constants.AppStatus;
import org.eclipse.codewind.intellij.ui.tasks.RestartRunModeTask;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RestartRunModeAction extends AbstractProjectDependentAction {

    public RestartRunModeAction() {
        super(message("RestartInRunMode"));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        CodewindApplication app = getSelection(e);
        if (app != null && app.isAvailable() && app.getProjectCapabilities().canRestart()) {
            e.getPresentation().setEnabled(app.getAppStatus() == AppStatus.STARTED || app.getAppStatus() == AppStatus.STARTING);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindApplication application = getSelection(e);
        if (application != null) {
            Project project = e.getProject();
            if (project != null) {
                boolean isCurrentOpenedProject = openProjectInIde(application, project);
                if (isCurrentOpenedProject) {
                    // Pass project to task
                    ProgressManager.getInstance().run(getTaskToRun(application, project));
                }
            }
        }
    }

    @Override
    protected Task.Backgroundable getTaskToRun(CodewindApplication application, Project project) {
        return new RestartRunModeTask(application, project);
    }
}
