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
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.ui.CodewindToolWindow;

public class CodewindToolWindowHelper {

    public static void openWindow(Project project) {
        CoreUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(CodewindToolWindow.ID);
                if (toolWindow != null && toolWindow.isAvailable()) {
                    toolWindow.show(null);
                }
            }
        });
    }
}
