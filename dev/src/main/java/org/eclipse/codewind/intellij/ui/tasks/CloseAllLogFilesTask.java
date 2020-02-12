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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.console.ProjectLogInfo;
import org.eclipse.codewind.intellij.ui.tree.CodewindToolWindowHelper;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class CloseAllLogFilesTask extends Task.Backgroundable {

    private final CodewindApplication application;
    private ToolWindow logFilesToolWindow;
    private ContentManager contentManager = null;

    public CloseAllLogFilesTask(CodewindApplication application, Project project) {
        super(project, message("ShowAllLogFilesAction"));
        this.application = application;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        logFilesToolWindow = toolWindowManager.getToolWindow(CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID);
        if (logFilesToolWindow != null) {
            contentManager = logFilesToolWindow.getContentManager();

            for (ProjectLogInfo logInfo : application.getLogInfos()) {
                Content content = contentManager.findContent(org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message("LogFileConsoleName" ,application.name, logInfo.logName));
                CoreUtil.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (content != null) {
                            contentManager.removeContent(content, true);
                        }
                    }
                });
            }
        }
    }
}
