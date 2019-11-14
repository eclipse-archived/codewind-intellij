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
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import java.util.Optional;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;

public abstract class TreeAction extends AnAction {
    public TreeAction(String text) {
        super(text);
    }

    protected  <T> Optional<T> selectionAsType(@NotNull AnActionEvent e, Class<T> type) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.log("unrecognized component for OpenIdeaProjectAction: " + data);
            System.out.println("*** unrecognized component for OpenIdeaProjectAction: " + data);
            return Optional.empty();
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        Object node = treePath.getLastPathComponent();
        if (!(type.isInstance(node))) {
            Logger.log("unrecognized node for OpenIdeaProjectAction: " + node);
            System.out.println("*** unrecognized node for OpenIdeaProjectAction: " + node);
            return Optional.empty();
        }
        return Optional.of(type.cast(node));
    }
}
