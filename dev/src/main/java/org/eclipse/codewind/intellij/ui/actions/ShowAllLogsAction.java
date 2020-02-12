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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.ui.tasks.ShowAllLogFilesTask;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ShowAllLogsAction extends AnAction {

    public ShowAllLogsAction() {
        super(message("ShowAllLogFilesAction"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindApplication application = getSelection(e);
        if (e != null) {
            // Pass project to task and on to overview
            ShowAllLogFilesTask task = new ShowAllLogFilesTask(application, e.getProject());
            ProgressManager.getInstance().run(task);
        }
    }

    private CodewindApplication getSelection(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            return null;
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        Object node = treePath.getLastPathComponent();
        if (!(node instanceof CodewindApplication)) {
            return null;
        }
        return (CodewindApplication)node;
    }
}
