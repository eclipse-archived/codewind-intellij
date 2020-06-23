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
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.TemplateUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.RepositoryInfo;
import org.eclipse.codewind.intellij.ui.templates.RepositoryManagementDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.List;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;


public class ManageReposAction extends AnAction {

    public ManageReposAction() {
        super(message("RepoMgmtActionLabel"));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        CodewindConnection connection = getSelection(e);
        e.getPresentation().setEnabled(connection != null && connection.isConnected());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindConnection connection = getSelection(e);
        final List<RepositoryInfo>[] repoListArray = new List[1];
        try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                try {
                    ProgressIndicator mon = new EmptyProgressIndicator();
                    repoListArray[0] = TemplateUtil.listTemplateSources(connection.getConid(), mon);
                } catch (Exception e1) {
                    Logger.logWarning("An error occurred trying to get the template sources for: " + connection.getName() + ": " + e1.getMessage());
                }
            }, message("RepoListTask", "connection"), false, e.getProject());
            RepositoryManagementDialog dialog = new RepositoryManagementDialog(e.getProject(), connection, repoListArray[0]);
            // Init must be done after
            dialog.initForm();
            boolean rc = dialog.showAndGet();
            if (rc) {
                dialog.updateRepos();
            }
        } catch (Exception ex) {
            CoreUtil.openDialog(true, message("RepoListErrorTitle"), message("RepoListErrorMsg", ex));
        }
    }

    private CodewindConnection getSelection(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.logDebug("Unrecognized component for : " + data);
            return null;
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.logDebug("No selection path for ManageReposAction: " + tree);
            return null;
        }
        Object node = treePath.getLastPathComponent();
        if (!(node instanceof CodewindConnection)) {
            return null;
        }
        return (CodewindConnection) node;
    }
}
