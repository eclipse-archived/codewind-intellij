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
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class AbstractCodewindWizard extends AbstractWizardEx implements ChangeListener {
    protected String helpUrl;

    public AbstractCodewindWizard(String title, Project project, List<? extends AbstractCodewindWizardStep> steps, String helpUrl) {
        super(title, project, steps);
        this.helpUrl = helpUrl;
    }

    @Nullable
    @Override
    protected String getHelpID() {
        return null;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    protected boolean canFinish() {
        return super.canFinish();
    }

    @Override
    protected void helpAction() {
        try {
            BrowserLauncher.getInstance().browse(new URI(helpUrl));
        } catch (URISyntaxException ex) {
            Logger.logWarning(ex);
        }
    }

    @Override
    public abstract void stateChanged(ChangeEvent changeEvent);
}
