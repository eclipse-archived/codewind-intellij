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

import com.intellij.icons.AllIcons;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.eclipse.codewind.intellij.ui.wizard.AbstractConnectionWizardStep;
import org.eclipse.codewind.intellij.ui.wizard.AddConnectionWizard;
import org.eclipse.codewind.intellij.ui.wizard.NewCodewindConnectionStep;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AddConnectionAction extends AnAction {

    public AddConnectionAction() {
        super(message("NewConnectionActionLabel"), message("NewConnectionPage_WizardDescription"), IconCache.getCachedIcon(IconCache.ICONS_ELCL16_NEW_REMOTE));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.log("unrecognized component for : " + data);
            return;
        }

        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("no selection for: " + data);
            return;
        }

        Object node = treePath.getLastPathComponent();
        CodewindTreeModel model = (CodewindTreeModel) tree.getModel();
        if (node instanceof ConnectionManager) {
            ConnectionManager connectionManager = (ConnectionManager)node;
            List<AbstractConnectionWizardStep> steps = new ArrayList<>();
            NewCodewindConnectionStep step = new NewCodewindConnectionStep(message("NewConnectionPage_RemoteStep"), e.getProject(), connectionManager);
            steps.add(step);
            AddConnectionWizard wizard = new AddConnectionWizard( message("NewConnectionPage_ShellTitle"), e.getProject(), steps, connectionManager);
            boolean rc = wizard.showAndGet();
            if (rc) {
                ProgressManager.getInstance().run(new RefreshTask(node, tree));
            }
        }
    }
}
