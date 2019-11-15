/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RefreshAction extends AnAction {

    public RefreshAction() {
        super(message("ACTION_REFRESH"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.log("unrecognized component for RefreshAction: " + data);
            System.out.println("*** unrecognized component for RefreshAction: " + data);
            return;
        }

        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("no selection for RefreshAction: " + data);
            System.out.println("*** no selection for RefreshAction: " + data);
            return;
        }

        Object node = treePath.getLastPathComponent();
        ProgressManager.getInstance().run(new RefreshTask(node, tree));
        CodewindTreeModel model = (CodewindTreeModel) tree.getModel();
        if (node instanceof ConnectionManager) {
            model.updateAll();
            return;
        }
        if (node instanceof CodewindConnection) {
            CodewindConnection connection = (CodewindConnection) node;
            model.updateConnection(connection);
            return;
        }
        if (node instanceof CodewindApplication) {
            model.updateApplication((CodewindApplication) node);
            return;
        }
    }
}
