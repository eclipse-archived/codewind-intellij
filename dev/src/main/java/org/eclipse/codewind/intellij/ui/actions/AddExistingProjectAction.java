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
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.wizard.AbstractBindProjectWizardStep;
import org.eclipse.codewind.intellij.ui.wizard.AddExistingProjectWizard;
import org.eclipse.codewind.intellij.ui.wizard.ConfirmProjectTypeStep;
import org.eclipse.codewind.intellij.ui.wizard.ProjectFolderStep;
import org.eclipse.codewind.intellij.ui.wizard.ProjectTypeSelectionStep;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;


public class AddExistingProjectAction extends AnAction {

    public AddExistingProjectAction() {
        super(message("BindActionLabel"));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Warning: Update code must execute fast
        CodewindConnection connection = getSelection(e);
        e.getPresentation().setEnabled(connection == null ? false : connection.isConnected());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindConnection connection = getSelection(e);
        if (connection == null) {
            return;
        }
        List<AbstractBindProjectWizardStep> steps = new ArrayList<>();
        steps.add(new ProjectFolderStep(message("SelectProjectPageName"), e.getProject(), connection));
        steps.add(new ConfirmProjectTypeStep(message("ProjectValidationPageTitle"), e.getProject(), connection));
        steps.add(new ProjectTypeSelectionStep(message("SelectProjectPageTitle"), e.getProject(), connection));
        AddExistingProjectWizard wizard = new AddExistingProjectWizard(message("BindProjectWizardTitle"), e.getProject(), steps, connection);
        wizard.showAndGet();
    }

    private CodewindConnection getSelection(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            return null;
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("No selection path for AddExistingProjectAction: " + tree);
            return null;
        }
        Object node = treePath.getLastPathComponent();
        if (!(node instanceof CodewindConnection)) {
            return null;
        }
        return (CodewindConnection)node;
    }
}
