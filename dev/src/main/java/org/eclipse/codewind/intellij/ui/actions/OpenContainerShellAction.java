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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.KubeUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.IOException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class OpenContainerShellAction extends AbstractProjectDependentAction {
    public OpenContainerShellAction() {
        super(message("ActionOpenContainerShell"));
    }

    @Override
    protected Task.Backgroundable getTaskToRun(CodewindApplication app, Project project) {
        return new Task.Backgroundable(project, message("ActionOpenContainerShell")) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // The shell must be opened from the UI thread.
                CoreUtil.invokeLater(this::run0);
            }

            public void run0() {
                // exec bash if it's installed, else exec sh
                String command = "sh -c \"if type bash > /dev/null; then bash; else sh; fi\"";

                // Open a shell in the application container
                String processPath = null;
                String processArgs = null;
                if (app.connection.isLocal()) {
                    String envPath = CoreUtil.getEnvPath();
                    processPath = envPath != null ? envPath + "docker" : "docker"; //$NON-NLS-1$ //$NON-NLS-2$
                    processArgs = "exec -it " + app.getContainerId() + " " + command; //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    processPath = KubeUtil.getCommand();
                    if (processPath == null) {
                        Logger.logWarning("The container shell cannot be opened because neither of the kubectl or oc commands could be found on the path");
                        CoreUtil.openDialog(true, message("ActionOpenContainerShellErrorTitle"), message("ActionOpenContainerShellNoKubectlMsg"));
                        return;
                    }
                    processArgs = "exec -n " + app.getNamespace() + " -it " + app.getPodName() + " -- " + command; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                String cmd = processPath + " " + processArgs;
                TerminalView terminalView = TerminalView.getInstance(myProject);
                try {
                    ShellTerminalWidget w = terminalView.createLocalShellWidget(null, app.getName());
                    w.executeCommand(cmd);
                    w.requestFocusInWindow();
                } catch (IOException e) {
                    Logger.logWarning(e);
                    CoreUtil.openDialog(true, message("ActionOpenContainerShellErrorTitle"), e.getLocalizedMessage());
                }
            }
        };
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindApplication application = getSelection(e);
        if (application == null)
            return;
        Project project = e.getProject();
        if (project == null)
            return;
        boolean isCurrentOpenedProject = openProjectInIde(application, project);
        if (isCurrentOpenedProject) {
            ProgressManager.getInstance().run(getTaskToRun(application, project));
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        CodewindApplication application = getSelection(e);
        e.getPresentation().setEnabled(shouldEnable(application));
    }

    private boolean shouldEnable(CodewindApplication application) {
        if (application == null)
            return false;
        if (!application.isAvailable())
            return false;
        if (application.connection.isLocal()) {
            return application.getContainerId() != null;
        }
        return application.getPodName() != null && application.getNamespace() != null;
    }
}
