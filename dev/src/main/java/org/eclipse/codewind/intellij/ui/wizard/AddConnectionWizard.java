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

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.wizard.AbstractWizardEx;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class AddConnectionWizard extends AbstractWizardEx {


    public AddConnectionWizard(String title, @Nullable Project project, List<? extends AbstractConnectionWizardStep> steps, ConnectionManager connectionManager) {
        super(title, project, steps);
    }

    @Nullable
    @Override
    protected String getHelpID() {
        return null;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        NewCodewindConnectionStep currentStepObject = (NewCodewindConnectionStep)getCurrentStepObject();
        currentStepObject.doOKAction();
    }

    @Override
    protected boolean canFinish() {
        boolean canFinish = super.canFinish();
        return canFinish;
    }

    protected void helpAction() {
        try {
            BrowserLauncher.getInstance().browse(new URI(UIConstants.REMOTE_DEPLOY_URL));
        } catch (URISyntaxException ex) {
            Logger.logWarning(ex);
        }
    }

}
