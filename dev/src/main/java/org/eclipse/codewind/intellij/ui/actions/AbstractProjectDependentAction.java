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

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.FileUtil;
import org.eclipse.codewind.intellij.ui.tree.CodewindToolWindowHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.JFrame;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

/**
 * This action is for any action that depends on the Codewind project being opened in the IDE.  If the currently opened project
 * is not opened, the user is prompted to open the project.
 */
public abstract class AbstractProjectDependentAction extends AbstractApplicationAction {

    public AbstractProjectDependentAction(String text) {
        super(text);
    }

    /**
     * If the currently opened project has the same location as the selected
     *
     * @param application - The Codewind application
     * @param project - The IntelliJ project
     * @return true if the project of the selected Codewind application is opened in the current IDE, false, otherwise
     */
    protected boolean openProjectInIde(@NotNull CodewindApplication application, @NotNull Project project) {
        String basePath = project.getBasePath().toString();
        String targetLocalPath = application.fullLocalPath.toString();
        CodewindApplication appByLocation = application.getConnection().getAppByLocation(basePath);
        if (appByLocation != application) {
            // Check if the project is already opened in a window
            Project[] openedProjects = ProjectManager.getInstance().getOpenProjects();
            for (int i = 0; i < openedProjects.length; i++) {
                Project openedProject = openedProjects[i];
                @SystemIndependent String openedProjectPath = openedProject.getBasePath();
                if (FileUtil.isSamePath(openedProjectPath, targetLocalPath)) {
                    final Project targetOpenedProject = openedProject;
                    // If the project is already opened, just set it active and perform the task
                    JFrame frame = WindowManager.getInstance().getFrame(targetOpenedProject);
                    if (frame != null) {
                        CoreUtil.invokeLater(() -> {
                            int rc = CoreUtil.showYesNoDialog(message("ProjectIsOpenedInAnotherWindowTitle"), message("ProjectIsOpenedInAnotherWindowMsg"));
                            if (rc == Messages.YES) {
                                frame.setVisible(true);
                                WindowManager.getInstance().suggestParentWindow(targetOpenedProject);
                                // Pass project to task
                                Task.Backgroundable task = getTaskToRun(application, targetOpenedProject);
                                ProgressManager.getInstance().run(task);
                            }
                        });
                    }
                    return false; // return false in this case
                }
            }
            // Otherwise prompt to open in new or current window
            CoreUtil.invokeLater(() -> {
                int rc = CoreUtil.showYesNoDialog(message("ProjectMustBeOpenedToDebugTitle"), message("ProjectMustBeOpenedToDebugMessage"));
                if (rc == Messages.YES) {
                    Project targetProject = com.intellij.ide.impl.ProjectUtil.openOrImport(application.fullLocalPath, project, false);
                    if (targetProject != null) {
                        CodewindToolWindowHelper.openWindow(targetProject); // Open the Codewind Tool Window
                        Task.Backgroundable task = getTaskToRun(application, targetProject);
                        ProgressManager.getInstance().run(task);
                    }
                }
            });
            return false;
        }
        return true;
    }

    // Task to run later after prompting the user to select another IDE window with the target project opened
    abstract protected Task.Backgroundable getTaskToRun(CodewindApplication application, Project project);
}
