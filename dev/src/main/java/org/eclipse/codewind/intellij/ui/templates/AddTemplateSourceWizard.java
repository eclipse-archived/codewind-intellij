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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;
import org.eclipse.codewind.intellij.ui.templates.form.AuthForm;
import org.eclipse.codewind.intellij.ui.wizard.AbstractCodewindWizard;
import org.eclipse.codewind.intellij.ui.wizard.AbstractCodewindWizardStep;
import org.eclipse.codewind.intellij.ui.wizard.CodewindCommitStepException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AddTemplateSourceWizard extends AbstractCodewindWizard {

    public AddTemplateSourceWizard(@Nullable Project project, List<? extends AbstractCodewindWizardStep> steps) {
        super(message("AddRepoDialogTitle"), project, steps, UIConstants.TEMPLATES_INFO_URL);
    }

    public AddTemplateSourceWizard(String title, @Nullable Project project, List<? extends AbstractCodewindWizardStep> steps) {
        super(title, project, steps, UIConstants.TEMPLATES_INFO_URL);
    }

    @Override
    protected void doNextAction() {
        try {
            setErrorText(null);
            AbstractAddTemplateSourceWizardStep currentStepObject = (AbstractAddTemplateSourceWizardStep) getCurrentStepObject();
            currentStepObject.onStepLeaving();
            // Get next step object reference for use after super.doNextAction();
            AbstractAddTemplateSourceWizardStep nextStepObject = (AbstractAddTemplateSourceWizardStep) getNextStepObject();
            nextStepObject.onStepEntering(); // Do calls prior to entering the page
            // Now, switch pages and show content
            super.doNextAction();
            // getNextStepObject() will return the next step, which isn't what we want
            nextStepObject.postDoNextStep(); // This could be for validation right after the page appears
        } catch (CodewindCommitStepException e) {
            CoreUtil.openDialog(true, e.getTitle(), e.getMessage());
            if (e.getComponent() != null) {
                setErrorText(e.getMessage(), e.getComponent());
            }
        }
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        return super.doValidateAll();
    }

    @Override
    protected void doPreviousAction() {
        // Thus far, only care about stepLeaving
        try {
            AbstractAddTemplateSourceWizardStep currentStepObject = (AbstractAddTemplateSourceWizardStep) getCurrentStepObject();
            currentStepObject.onStepLeaving();
            super.doPreviousAction();
        } catch (CommitStepException e) {

        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
    
    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        Object source = changeEvent.getSource();
        if (source instanceof JComponent) {
            JComponent component = (JComponent) source;
            if (hasErrors(component)) {
                setErrorText(null, (JComponent) source);
            }
        } else if (source == AuthForm.AUTH_SUCCESSFUL) {
            setErrorText(null);
        }
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return super.createActions();
    }

    @Nullable
    protected ValidationInfo doValidate() {
        AbstractAddTemplateSourceWizardStep currentStepObject = (AbstractAddTemplateSourceWizardStep) getCurrentStepObject();
        return currentStepObject.doValidate();
    }

    public RepoEntry getRepoEntry() {
        // We know the order of the steps.
        UrlStep urlStep = (UrlStep)mySteps.get(0);
        DetailsStep detailsStep = (DetailsStep)mySteps.get(1);
        String url = urlStep.getTemplateSourceUrl();
        String name = detailsStep.getTemplateSourceName();
        String description = detailsStep.getTemplateSourceDescription();
        if (name != null && !name.isEmpty() &&
                description != null && !description.isEmpty() &&
                url != null && !url.isEmpty()) {
            return new RepoEntry(url, urlStep.getUsername(), urlStep.getPassword(), urlStep.getToken(), name, description);
        }
        return null;
    }

}
