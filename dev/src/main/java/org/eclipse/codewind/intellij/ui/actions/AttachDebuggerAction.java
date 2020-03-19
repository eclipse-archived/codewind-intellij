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
import org.eclipse.codewind.intellij.core.constants.StartMode;
import org.eclipse.codewind.intellij.ui.tasks.AttachDebuggerTask;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AttachDebuggerAction extends AbstractProjectDependentAction {

    public AttachDebuggerAction() {
        super(message("AttachDebuggerLabel"));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        CodewindApplication app = getSelection(e);
        if (app.isAvailable() && StartMode.DEBUG_MODES.contains(app.getStartMode()) && app.getDebugPort() != -1 &&
                (app.getAppStatus() == AppStatus.STARTED || app.getAppStatus() == AppStatus.STARTING)) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindApplication application = getSelection(e);
        if (application != null) {
            Project project = e.getProject();
            boolean isCurrentOpenedProject = openProjectInIde(application, project);
            if (isCurrentOpenedProject) {
                ProgressManager.getInstance().run(getTaskToRun(application, project));
            }
        }
    }

    @Override
    protected Task.Backgroundable getTaskToRun(CodewindApplication application, Project project) {
        return new AttachDebuggerTask(application, project);
    }
}
