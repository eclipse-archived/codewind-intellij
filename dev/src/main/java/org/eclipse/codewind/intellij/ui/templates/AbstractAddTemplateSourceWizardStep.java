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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.wizard.AbstractCodewindWizardStep;
import org.eclipse.codewind.intellij.ui.wizard.CodewindCommitStepException;

public abstract class AbstractAddTemplateSourceWizardStep extends AbstractCodewindWizardStep {

    protected CodewindConnection connection;
    protected Project project;
    protected AddTemplateSourceWizardModel wizardModel;

    public AbstractAddTemplateSourceWizardStep(String title) {
        super(null); // set this to null to override behavior
    }

    protected abstract void onStepEntering();

    protected abstract void onStepLeaving() throws CodewindCommitStepException;

    protected abstract void postDoNextStep();

    public abstract ValidationInfo doValidate();
}