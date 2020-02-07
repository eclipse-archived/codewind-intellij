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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ProjectTypeInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectType;
import org.eclipse.codewind.intellij.ui.form.ProjectTypeSelectionForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTypeSelectionStep  extends AbstractBindProjectWizardStep {
    public static String STEP_ID = "ProjectTypeSelectionStep";
    private ProjectTypeSelectionForm form;
    private Project project;
    private CodewindConnection connection;
    private ProjectInfo initialProjectInfo;
    public ProjectTypeSelectionStep(@Nullable String title, Project project, CodewindConnection connection) {
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
        return null;
    }

    @Nullable
    @Override
    public Object getPreviousStepId() {
        return ConfirmProjectTypeStep.STEP_ID;
    }

    @Override
    public boolean isComplete() {
        ProjectTypeInfo selectedProjectTypeInfo = form.getSelectedProjectTypeInfo();
        ProjectTypeInfo.ProjectSubtypeInfo selectedSubtype = form.getSelectedSubtype();
        if (selectedProjectTypeInfo != null) {
            // for docker type language selection is optional
            // for non-docker when selected type is different than the detected type,
            // user need to choose a subtype to proceed
            if (!selectedProjectTypeInfo.eq(ProjectType.TYPE_DOCKER) && (initialProjectInfo == null || !selectedProjectTypeInfo.eq(initialProjectInfo.type))) {
                return selectedSubtype != null;
            } else {
                return true;
            }
        }
        return false;
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
        form = new ProjectTypeSelectionForm(this);
        return form.getContentPane();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    public void updateProjectTypeMap(BindProjectModel model) {
        if (model.getTypes() == null) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
                @Override
                public void run() {
                    ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                    Map<String, ProjectTypeInfo> types = getProjectTypeMap();
                    model.setTypes(types);
                    form.updateProjectTypesList(true, types);
                    form.setProjectTypes(types);
                }
            }, "Getting project types", false, project);
        }
    }

    private Map<String, ProjectTypeInfo> getProjectTypeMap() {
        List<ProjectTypeInfo> projectTypes = null;
        Map<String, ProjectTypeInfo> typeMap = new HashMap<>();
        try {
            projectTypes = connection.requestProjectTypes();
        } catch (Exception e) {
            return typeMap;
        }
        if (projectTypes == null || projectTypes.isEmpty()) {
            return typeMap;
        }
        for (ProjectTypeInfo projectType : projectTypes) {
            ProjectTypeInfo existing = typeMap.get(projectType.getId());
            if (existing == null) {
                typeMap.put(projectType.getId(), projectType);
            }
            else {
                existing.addSubtypes(projectType.getSubtypes());
            }
        }
        return typeMap;
    }

    private void setInitialProjectInfo(ProjectInfo initialProjectInfo) {
        this.initialProjectInfo = initialProjectInfo;
        form.setInitialProjectInfo(this.initialProjectInfo);
    }

    @Override
    protected void onStepEntering(BindProjectModel model) {
        setInitialProjectInfo(model.getProjectInfo());
        updateProjectTypeMap(model);
    }

    @Override
    protected void onStepLeaving(BindProjectModel model) {
        model.setProjectTypeInfo(form.getSelectedProjectTypeInfo());
        model.setSubtypeInfo(form.getSelectedSubtype());
    }

    @Override
    protected void postDoNextStep(BindProjectModel model) {
        // empty
    }
}
