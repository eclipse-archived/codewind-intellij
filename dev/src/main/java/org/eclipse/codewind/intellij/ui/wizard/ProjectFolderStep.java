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
package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.form.AddExistingProjectForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProjectFolderStep extends AbstractBindProjectWizardStep {
    public static String STEP_ID = "ProjectFolderStep";
    private AddExistingProjectForm form;
    private Project project;
    private CodewindConnection connection;

    public ProjectFolderStep(@Nullable String title, Project project, CodewindConnection connection) {
        super(title);
        this.project = project;
        this.connection = connection;
    }

    @NotNull
    @Override
    public Object getStepId() {
        return STEP_ID;
    }

    @Nullable
    @Override
    public Object getNextStepId() {
        return ConfirmProjectTypeStep.STEP_ID;
    }

    @Nullable
    @Override
    public Object getPreviousStepId() {
        return null;
    }

    @Override
    public boolean isComplete() {
        return !(form.getProjectPath().isEmpty()) && !form.isProjectAlreadyAdded();
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
        // Empty. Use onStepLeaving instead.
    }

    @Override
    public JComponent getComponent() {
        // We don't want to recreate the widgets again.  getComponent gets called multiple times by the wizard framework
        if (form != null) {
            return form.getContentPane();
        }
        form = new AddExistingProjectForm(this, this.project, connection);
        // Attempt to initialize the path with the currently opened project's path
        String basePath = project.getBasePath();
        boolean isCodewindProject = form.isCodewindProject(basePath);
        if (!isCodewindProject) {
            form.setProjectPath(basePath);
        }
        return form.getContentPane();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    public String getProjectPath() {
        return form.getProjectPath();
    }

    @Override
    protected void onStepEntering(BindProjectModel model) {
        // empty
    }

    @Override
    protected void onStepLeaving(BindProjectModel model) {
        if (!this.getProjectPath().equals(model.getProjectPath())) {
            model.setProjectTypeInfo(null);
            model.setSubtypeInfo(null);
        }
        model.setProjectPath(this.getProjectPath());
    }

    @Override
    protected void postDoNextStep(BindProjectModel model) {
        // empty
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        // ignore
    }

    @Override
    public void addListener(ChangeListener listener) {
        // ignore
    }
}
