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
package org.eclipse.codewind.intellij.ui.tree;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.ui.CodewindToolWindow;

import javax.swing.JComponent;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class CodewindToolWindowHelper {

    // ID for the Log Files Tool Window
    public final static String SHOW_LOG_FILES_TOOLWINDOW_ID = "org.eclipse.codewind.intellij.ui.logFilesToolWindow";

    /**
     * Open and expand to project
     * @param project
     */
    public static void openWindow(Project project) {
        CoreUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(CodewindToolWindow.ID);
                if (toolWindow != null && toolWindow.isAvailable()) {
                    toolWindow.show(null); // Keep this null
                    ContentManager contentManager = toolWindow.getContentManager();
                    final Content content = contentManager.findContent(CodewindToolWindow.DISPLAY_NAME);
                    JComponent component = content.getComponent();
                    if (content != null && component instanceof CodewindToolWindow) {
                        CodewindToolWindow codewindToolWindow = (CodewindToolWindow)component;
                        codewindToolWindow.expandToProject(project);
                    }
                }
            }
        });
    }

    /**
     * Open Codewind window
     * @param project
     */
    public static void openCodewindWindow(Project project) {
        CoreUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(CodewindToolWindow.ID);
                if (toolWindow != null && toolWindow.isAvailable()) {
                    toolWindow.show(null); // Keep this null
                }
            }
        });
    }

    /**
     * Set the project to be selected prior to the window being shown or opened, which can be much later
     * @param project
     */
    public static void setInitialProjectToSelect(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(CodewindToolWindow.ID);
        if (toolWindow != null && toolWindow.isAvailable()) {
            ContentManager contentManager = toolWindow.getContentManager();
            final Content content = contentManager.findContent(CodewindToolWindow.DISPLAY_NAME);
            JComponent component = content.getComponent();
            if (content != null && component instanceof CodewindToolWindow) {
                CodewindToolWindow codewindToolWindow = (CodewindToolWindow)component;
                codewindToolWindow.setInitialSelectedProject(project);
            }
        }
    }
}
