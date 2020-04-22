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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.constants.AppStatus;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class OpenAppMonitorAction extends AbstractApplicationAction {

    public OpenAppMonitorAction() {
        super(message("ActionOpenMetricsDashboard"));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        CodewindApplication app = getSelection(e);
        e.getPresentation().setEnabled(
                app.isAvailable() &&
                        (app.getAppStatus() == AppStatus.STARTING || app.getAppStatus() == AppStatus.STARTED));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindApplication app = getSelection(e);
        Tree tree = getTree(e);
        if (app != null && tree != null) {
            ProgressManager.getInstance().run(new RefreshTask(app, tree));
            final URL perfURL = app.getMetricsDashboardUrl();
            if (perfURL != null) {
                URI perfURI = null;
                try {
                    perfURI = perfURL.toURI();
                    com.intellij.ide.browsers.BrowserLauncher.getInstance().browse(perfURI);
                } catch (URISyntaxException use) {
                    Logger.log("Bad Metrics Dashboard URL: " + perfURL);
                }
            }
            return;
        }
    }
}