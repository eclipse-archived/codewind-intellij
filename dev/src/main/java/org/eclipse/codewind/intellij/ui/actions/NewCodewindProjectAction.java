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

import com.intellij.ide.actions.NewProjectAction;
import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.ide.projectWizard.NewProjectWizard;
import com.intellij.ide.projectWizard.ProjectTypeStep;
import com.intellij.ide.util.newProjectWizard.TemplatesGroup;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.module.CodewindModuleType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.List;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class NewCodewindProjectAction extends NewProjectAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        NewProjectWizard wizard = new NewProjectWizard(null, ModulesProvider.EMPTY_MODULES_PROVIDER, null);
        try {
            selectCodewindTemplate(wizard);
        } catch (Exception ex) {
            Logger.logWarning(ex);
        }
        NewProjectUtil.createNewProject(wizard);
    }

    private void selectCodewindTemplate(NewProjectWizard wizard) {
        List<ModuleWizardStep> steps = wizard.getSequence().getAllSteps();
        for (ModuleWizardStep step: steps) {
            if (step instanceof ProjectTypeStep) {
                ProjectTypeStep projectTypeStep = (ProjectTypeStep) step;
                if (projectTypeStep.getPreferredFocusedComponent() instanceof JBList) {
                    @SuppressWarnings("unchecked")
                    JBList<TemplatesGroup> list = ((JBList<TemplatesGroup>)projectTypeStep.getPreferredFocusedComponent());
                    ListModel<TemplatesGroup> model = (ListModel<TemplatesGroup>) list.getModel();
                    for (int i = 0; i < model.getSize(); i++) {
                        TemplatesGroup group = model.getElementAt(i);
                        if (CodewindModuleType.CODEWIND_MODULE.equals(group.getId())) {
                            list.setSelectedIndex(i);
                            return;
                        }
                    }
                }
                return;
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        CodewindConnection connection = getSelection(e);
        e.getPresentation().setText(message("NewProjectAction_Label"), true);
        e.getPresentation().setEnabled(connection != null && connection.isConnected());
    }

    private CodewindConnection getSelection(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            return null;
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("No selection path for NewCodewindProjectAction: " + tree);
            return null;
        }
        Object node = treePath.getLastPathComponent();
        if (!(node instanceof CodewindConnection)) {
            return null;
        }
        return (CodewindConnection) node;
    }
}
