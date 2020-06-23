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
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.ThrowableComputable;
import org.eclipse.codewind.intellij.core.HttpUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.templates.form.UrlForm;
import org.eclipse.codewind.intellij.ui.wizard.CodewindCommitStepException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class UrlStep extends AbstractAddTemplateSourceWizardStep {
    public static String STEP_ID = "UrlStep";
    private UrlForm form;

    public UrlStep(Project project, CodewindConnection connection, AddTemplateSourceWizardModel wizardModel) {
        super(null); // "Url of Template Source Repository");
        this.project = project;
        this.connection = connection;
        this.wizardModel = wizardModel;
        this.nextStepId = DetailsStep.STEP_ID;  // Auth Checkbox is unchecked by default
    }

    @NotNull
    @Override
    public Object getStepId() {
        return STEP_ID;
    }

    @Nullable
    @Override
    public Object getNextStepId() {
        return nextStepId;
    }

    @Nullable
    @Override
    public Object getPreviousStepId() {
        return null;
    }

    @Override
    public boolean isComplete() {
        return form.isComplete();
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
        // empty
    }

    @Override
    public JComponent getComponent() {
        if (form == null) {
            form = new UrlForm(project, connection, false, wizardModel.getTemplateSourceURL());
            // Add this step as a listener to any form changes
            form.addListener(this);
            form.getAuthForm().addListener(this);
        }
        return form.getContentPane();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    /**
     * The step is a listener to any form widget changes so that steps can change their next page target,
     * amongst other things. Validation should not be done.
     */
    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        fireStateChanged();
    }

    @Override
    protected void onStepEntering() {
        // empty
    }

    // Do necessary validation before going to the next page.  If error, stay on current page
    @Override
    protected void onStepLeaving() throws CodewindCommitStepException {
        String urlValue = form.getTemplateSourceUrl();
        JTextField urlTextField = form.getTemplateSourceTextField();
        if (urlTextField.getText().length() == 0) {
            throw new CodewindCommitStepException(message("AddRepoDialogInvalidUrlTitle"), message("AddRepoDialogInvalidUrlMsg"), urlTextField);
        }
        try {
            if (urlValue != null) {
                URI uri = new URI(urlValue);
            }
        } catch (URISyntaxException e) {
            throw new CodewindCommitStepException(message("AddRepoDialogInvalidUrlTitle"), message("AddRepoDialogInvalidUrlMsg"), urlTextField);
        }

        wizardModel.setUrlValue(getTemplateSourceUrl());
        wizardModel.setAuthInfo(form.getAuthInfo());

        if (!wizardModel.isEdit()) {
            List<RepoEntry> repoEntries = wizardModel.getRepoEntries();
            for (RepoEntry entry : repoEntries) {
                if (form.getTemplateSourceUrl().equals(entry.url)) {
                    throw new CodewindCommitStepException(message("AddRepoDialogInvalidUrlTitle"),
                            message("AddRepoDialogDuplicateUrlError"),
                            form.getTemplateSourceTextField());
                }
            }
        }

        // Test connection again in case the URL was changed
        try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(new ThrowableComputable<Object, Exception>() {
                @Override
                public Object compute() throws Exception {
                    ProgressIndicator monitor = new EmptyProgressIndicator();
                    HttpUtil.HttpResult result = HttpUtil.get(new URI(urlValue), form.getAuthInfo());
                    if (!result.isGoodResponse) {
                        String errorMsg = result.error;
                        if (errorMsg == null || errorMsg.trim().isEmpty()) {
                            errorMsg = message("AddRepoDialogTestFailedDefaultMsg", result.responseCode);
                        }
                        throw new InvocationTargetException(new IOException(errorMsg));
                    }
                    return result;
                }
            }, message("AddRepoDialogTestTaskLabel", urlValue), true, project);
        } catch (Exception e) {
            String msg = e instanceof InvocationTargetException ? ((InvocationTargetException)e).getCause().toString() : e.toString();
            throw new CodewindCommitStepException(message("AddRepoDialogTestFailedTitle"),  message("AddRepoDialogTestFailedError", msg), urlTextField);
        }
    }

    @Override
    protected void postDoNextStep() {
        // empty
    }

    @Override
    public ValidationInfo doValidate() {
        return form.doValidate();
    }

    public String getTemplateSourceUrl() {
        return form.getTemplateSourceUrl();
    }

    String getUsername() {
        return form.getUsername();
    }

    String getPassword() {
        return form.getPassword();
    }

    String getToken() {
        return form.getToken();
    }

    // Add any interested listener to any form changes
    @Override
    public void addListener(ChangeListener listener) {
        form.addListener(listener);
        form.getAuthForm().addListener(listener);
    }
}
