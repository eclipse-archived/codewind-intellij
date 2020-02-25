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
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.ui.tasks.RemoveProjectTask;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RemoveProjectAction extends AnAction {

    public RemoveProjectAction() {
        super(message("UnbindActionLabel"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.log("unrecognized component for RemoveProjectAction: " + data);
            System.out.println("*** unrecognized component for RemoveProjectAction: " + data);
            return;
        }

        Tree tree = (Tree) data;
        TreePath[] treePaths = tree.getSelectionPaths();
        List<CodewindApplication> applications = new ArrayList<>();
        if (treePaths != null) {
            for (TreePath treePath:  treePaths) {
                Object node = treePath.getLastPathComponent();
                if (node instanceof CodewindApplication) {
                    applications.add((CodewindApplication) node);
                } else {
                    Logger.log("selection for RemoveProjectAction is not a project: " + data);
                    System.out.println("*** selection for RemoveProjectAction is not a project: " + data);
                }
            }
        }
        if (applications.isEmpty()) {
            Logger.log("no selection for RemoveProjectAction: " + data);
            System.out.println("*** no selection for RemoveProjectAction: " + data);
            return;
        }

        String title = message("UnbindActionTitle");
        String message;
        if (applications.size() == 1) {
            message = message("UnbindActionMessage", applications.get(0).name);
        } else {
            message = message("UnbindActionMultipleMessage", applications.size());
        }
        int response = Messages.showConfirmationDialog(tree, message, title, Messages.OK_BUTTON, Messages.CANCEL_BUTTON);
        if (response == Messages.OK) {
            ProgressManager.getInstance().run(new RemoveProjectTask(applications));
        }
    }
}
