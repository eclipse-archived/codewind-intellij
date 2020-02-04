/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.eclipse.codewind.intellij.core.constants.IntelliJConstants.IDEA_FOLDER;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class OpenIdeaProjectTask extends Task.Backgroundable {

    private final CodewindApplication application;

    public OpenIdeaProjectTask(CodewindApplication application) {
        super(null, message("ProjectOpenJob", application.getName()));
        this.application = application;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        try {
            run0(indicator);
        } catch (Exception e) {
            // Handled in #onThrowable()
            throw new RuntimeException(e);
        }
    }

    private void run0(@NotNull ProgressIndicator indicator) throws IOException {
        ProjectManagerEx pm = ProjectManagerEx.getInstanceEx();
        Path path = application.ideaProjectPath();
        if (path == null) {
            // Codewind project is not an IntelliJ project.
            System.out.println("*** creating idea project for: application" + application.getName());
            // If we don't create the '.idea' folder, the openOrImport() call fails (but weirdly, only
            // the first time)
            Files.createDirectory(application.fullLocalPath.resolve(IDEA_FOLDER));
            String project = application.fullLocalPath.toString();
            CoreUtil.invokeLater(
                    () -> {
                        ProjectUtil.openOrImport(project, null, false);
                    }
            );
            return;
        }
        System.out.println("*** loading idea project for: application" + application.getName());
        Project project = pm.loadProject(path);
        if (project == null) {
            System.out.println("*** no project returned for application " + application.getName());
        } else if (!project.isOpen()) {
            Runnable runner = () -> pm.openProject(project);
            TransactionGuard.getInstance().submitTransactionAndWait(runner);
        }
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        Throwable t = error;
        while (t.getCause() != null)
            t = t.getCause();
        Logger.logWarning("An error occurred opening project " + application.getName(), t);
        Messages.showErrorDialog(message("ProjectOpenError", application.getName()), "Codewind");
    }
}
