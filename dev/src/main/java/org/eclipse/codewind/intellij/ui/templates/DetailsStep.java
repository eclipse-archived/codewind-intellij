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
package org.eclipse.codewind.intellij.ui.templates;

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import org.eclipse.codewind.intellij.core.HttpUtil;
import org.eclipse.codewind.intellij.core.HttpUtil.HttpResult;
import org.eclipse.codewind.intellij.core.IAuthInfo;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.templates.form.DetailsForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.net.URL;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class DetailsStep extends AbstractAddTemplateSourceWizardStep {
    public static String STEP_ID = "DetailsStep";
    private DetailsForm form;

    public static final String DETAILS_FILE_NAME = "templates.json";
    public static final String NAME_KEY = "name";
    public static final String DESCRIPTION_KEY = "description";

    private String nameValue, descriptionValue;
    private String[] defaultValues = new String[2];

    public DetailsStep(Project project, CodewindConnection connection, AddTemplateSourceWizardModel wizardModel) {
        this(project, connection, wizardModel, wizardModel.getNameValue(), wizardModel.getDescriptionValue());
    }

    public DetailsStep(Project project, CodewindConnection connection, AddTemplateSourceWizardModel wizardModel, String nameValue, String descriptionValue) {
        super(null); //"Details");
        this.project = project;
        this.connection = connection;
        this.wizardModel = wizardModel;
        this.nextStepId = null; // This won't change
        this.previousStepId = UrlStep.STEP_ID; // By default, the auth checkbox is unchecked
        this.nameValue = nameValue;
        this.descriptionValue = descriptionValue;
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
    } // This is the last page

    @Nullable
    @Override
    public Object getPreviousStepId() {
        return previousStepId;
    }

    @Override
    public boolean isComplete() {
        return form.isComplete();
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
        // ignore
    }

    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new DetailsForm(project, connection, nameValue, descriptionValue);
            form.addListener(this);
        }
        return form.getContentPane();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        fireStateChanged();
    }

    @Override
    protected void onStepEntering() {
        IAuthInfo authInfo = wizardModel.getAuthInfo();
        descriptionValue = wizardModel.getDescriptionValue();
        nameValue = wizardModel.getNameValue();

        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlValue = wizardModel.getTemplateSourceURL();
                    URL repoUrl = new URL(urlValue);
                    String path = repoUrl.getPath();
                    path = path.substring(0, path.lastIndexOf("/") + 1) + DETAILS_FILE_NAME;
                    URL detailsUrl = new URL(repoUrl.getProtocol(), repoUrl.getHost(), path);

                    // Try to get the template source details
                    HttpResult result = HttpUtil.get(detailsUrl.toURI(), authInfo);
                    if (result.isGoodResponse && result.response != null && !result.response.isEmpty()) {
                        JSONObject jsonObj = new JSONObject(result.response);
                        String name = jsonObj.has(NAME_KEY) ? jsonObj.getString(NAME_KEY) : null;
                        String description = jsonObj.has(DESCRIPTION_KEY) ? jsonObj.getString(DESCRIPTION_KEY) : null;

                        // The name should at least be set
                        if (name == null || name.isEmpty()) {
                            Logger.logDebug("Found the template source information but the name is null or empty: " + detailsUrl);
                        } else {
                            defaultValues[0] = name;
                            defaultValues[1] = description;
                            form.setDefaultValues(defaultValues);
                            if (nameValue == null) {
                                form.setNameValue(name);
                            } else {
                                form.setNameValue(nameValue);
                            }
                            if (descriptionValue == null) {
                                form.setDescriptionValue(description == null ? "" : description);
                            } else {
                                form.setDescriptionValue(descriptionValue);
                            }
                        }
                    } else {
                        // Don't log this as an error as the template source may not provide details
                        Logger.logTrace("Got error code " + result.error
                                + " trying to retrieve the template source details for url: " + detailsUrl
                                + ", and error: " + result.error);
                    }
                } catch (Exception e) {

                }
            }
        }, message("AddRepoDialogAutoFillTaskLabel"), true, project);
    }

    @Override
    protected void onStepLeaving() {
        // empty no extra actions
    }

    @Override
    protected void postDoNextStep() {
        // empty, last step
    }

    @Override
    public ValidationInfo doValidate() {
        return null;
    }

    String getTemplateSourceName() {
        return form.getTemplateSourceName();
    }

    String getTemplateSourceDescription() {
        return form.getTemplateSourceDescription();
    }

    @Override
    public void addListener(ChangeListener listener) {
        // empty
    }
}
