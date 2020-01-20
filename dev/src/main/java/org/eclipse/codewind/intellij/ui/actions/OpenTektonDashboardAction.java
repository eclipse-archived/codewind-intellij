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
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.ConnectionEnv;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.net.URISyntaxException;
import java.net.URL;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;

public class OpenTektonDashboardAction extends AnAction {

    public OpenTektonDashboardAction() {
        super("Open Tekton Dashboard");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.log("unrecognized component for OpenTektonDashboardAction: " + data);
            System.out.println("*** unrecognized component for OpenTektonDashboardAction: " + data);
            return;
        }

        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("no selection for OpenTektonDashboardAction: " + data);
            System.out.println("*** no selection for OpenTektonDashboardAction: " + data);
            return;
        }

        Object node = treePath.getLastPathComponent();
        ProgressManager.getInstance().run(new RefreshTask(node, tree));
        CodewindTreeModel model = (CodewindTreeModel) tree.getModel();
        if (node instanceof CodewindApplication) {
            CodewindApplication app = (CodewindApplication)node;
            ConnectionEnv.TektonDashboard tekton = app.getConnection().getTektonDashboard();
            if (tekton.hasTektonDashboard()) {
                final URL tektonURL = tekton.getTektonUrl();
                if (tektonURL != null) {
                    try {
                        com.intellij.ide.browsers.BrowserLauncher.getInstance().browse(tektonURL.toURI());
                    } catch (URISyntaxException use) {
                        Logger.log("Bad Tekton Dashboard URL: " + tektonURL);
                        System.out.println("*** Bad Tekton Dashboard URL: " + tektonURL);
                    }
                }
            }
            return;
        }
    }
}
