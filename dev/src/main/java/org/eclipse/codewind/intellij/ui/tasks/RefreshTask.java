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

package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.jetbrains.annotations.NotNull;

public class RefreshTask extends Task.Backgroundable {

    private final Object component;
    private final Tree tree;

    public RefreshTask(Object component, Tree tree) {
        super(null, "Refresh");
        this.component = component;
        this.tree = tree;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        CodewindTreeModel model = (CodewindTreeModel) tree.getModel();
        if (component instanceof ConnectionManager) {
            CoreUtil.invokeLater(() -> model.updateAll());
            return;
        }
        if (component instanceof LocalConnection) {
            LocalConnection connection = (LocalConnection) component;
            connection.refreshInstallStatus();
            if (connection.isConnected()) {
                connection.refreshApps(null);
            }
            CoreUtil.invokeLater(() -> model.updateConnection(connection));
            return;
        }
        if (component instanceof CodewindApplication) {
            CodewindApplication application = (CodewindApplication) this.component;
            application.getConnection().refreshApps(application.projectID);
            CoreUtil.invokeLater(() -> model.updateApplication(application));
            return;
        }

    }
}
