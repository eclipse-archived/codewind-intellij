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
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.net.URI;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RemoveConnectionAction extends AnAction {

    public RemoveConnectionAction() {
        super(message("RemoveConnectionActionLabel"));
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
        ProgressManager.getInstance().run(new RefreshTask(node, tree));
        CodewindTreeModel model = (CodewindTreeModel) tree.getModel();
        if (node instanceof RemoteConnection) {
            CodewindConnection remoteConnection = (CodewindConnection)node;
            String name = remoteConnection.getName();
            URI baseURI = remoteConnection.getBaseURI();
            CoreUtil.invokeLater(() -> {
                int rc = CoreUtil.showYesNoDialog(message("RemoveConnectionActionConfirmTitle"), message("RemoveConnectionActionConfirmMsg", name, baseURI.toString()) + "\n");
                if (rc == Messages.YES) {
                    try {
                        ConnectionManager.remove(remoteConnection.getBaseURI().toString());
                    } catch (Exception ex) {
                        Logger.logWarning("Error removing connection: " + remoteConnection.getBaseURI().toString(), ex); //$NON-NLS-1$
                    }

                }
            });
        }

    }
}
