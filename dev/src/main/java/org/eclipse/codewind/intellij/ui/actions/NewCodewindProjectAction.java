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

import com.intellij.ide.actions.NewProjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;

public class NewCodewindProjectAction extends NewProjectAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        CodewindConnection connection = getSelection(e);
        e.getPresentation().setEnabled(connection != null && connection.isConnected());
    }

    private CodewindConnection getSelection(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            return null;
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("No selection path for NewCodewindProjectAction: " + tree);
            return null;
        }
        Object node = treePath.getLastPathComponent();
        if (!(node instanceof CodewindConnection)) {
            return null;
        }
        return (CodewindConnection) node;
    }
}
