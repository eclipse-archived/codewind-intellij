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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.ui.tasks.EnableDisableInjectMetricsTask;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class EnableDisableInjectMetricsAction extends AbstractApplicationAction {

    public EnableDisableInjectMetricsAction(boolean enable) {
        super(enable ? message("EnableInjectMetricsLabel") : message("DisableInjectMetricsLabel"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindApplication app = getSelection(e);
        Tree tree = getTree(e);
        if (app != null && tree != null) {
            Presentation presentation = getTemplatePresentation();
            String text = presentation.getText();
            ProgressManager.getInstance().run(new EnableDisableInjectMetricsTask(app, e.getProject(), text));
            ProgressManager.getInstance().run(new RefreshTask(app, tree));
        }
    }
}
