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
import org.eclipse.codewind.intellij.ui.wizard.AbstractCodewindWizardStep;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class EditTemplateSourceWizard extends AddTemplateSourceWizard {

    public EditTemplateSourceWizard(@Nullable Project project, List<? extends AbstractCodewindWizardStep> steps) {
        super(message("EditRepoDialogTitle"), project, steps);
    }
}
