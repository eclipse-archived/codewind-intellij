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
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.ui.tasks.AutoBuildTask;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class DisableAutoBuildAction extends TreeAction<CodewindApplication> {

    public DisableAutoBuildAction() {
        super(message("DisableAutoBuildLabel"), CodewindApplication.class, AutoBuildTask::createDisabler);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Set the menu item enablement based on whether the application is available
        getSelection(e).ifPresent(application -> e.getPresentation().setEnabled(application.isAvailable()));
    }
}
