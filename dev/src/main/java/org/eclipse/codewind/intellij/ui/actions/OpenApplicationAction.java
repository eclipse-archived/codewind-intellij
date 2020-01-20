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
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;

public class OpenApplicationAction extends AnAction {

    public OpenApplicationAction() {
        super("Open Application");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.log("unrecognized component for OpenApplicationAction: " + data);
            System.out.println("*** unrecognized component for OpenApplicationAction: " + data);
            return;
        }

        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("no selection for OpenApplicationAction: " + data);
            System.out.println("*** no selection for OpenApplicationAction: " + data);
            return;
        }

        Object node = treePath.getLastPathComponent();
        ProgressManager.getInstance().run(new RefreshTask(node, tree));
        CodewindTreeModel model = (CodewindTreeModel) tree.getModel();
        if (node instanceof CodewindApplication) {
            CodewindApplication app = (CodewindApplication)node;
            final URL rootURL = app.getRootUrl();
            if (rootURL != null) {
                URI rootURI = null;
                try {
                    rootURI = rootURL.toURI();
                    com.intellij.ide.browsers.BrowserLauncher.getInstance().browse(rootURI);
                } catch (URISyntaxException use) {
                    Logger.log("Bad Application URL: " + rootURL);
                    System.out.println("*** Bad Application URL: " + rootURL);
                }
            }
            return;
        }
    }
}
