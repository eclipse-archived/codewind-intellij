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
package org.eclipse.codewind.intellij.ui.form;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.eclipse.codewind.intellij.core.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Extensible dialog provides extenders a Help button (that does not launch the Jetbrains help), a Cancel and an OK button.
 * Extenders should implement createCenterPanel() to provide custom widget content
 */
public abstract class AbstractCodewindDialogWrapper extends DialogWrapper {

    private String helpUrl;

    public AbstractCodewindDialogWrapper(Project project, String title, String helpUrl) {
        super(project,true); // Can use current dialog window as parent for other child dialogs
        init();
        setTitle(title);
        this.helpUrl = helpUrl;
    }

    @Nullable
    @Override
    protected String getHelpId() {
        return ""; // Must not be null otherwise the help button question icon will not appear
    }

    @Override
    protected void doHelpAction() {
        try {
            BrowserLauncher.getInstance().browse(new URI(helpUrl));
        } catch (URISyntaxException ex) {
            Logger.logDebug("Exception when lanching browser", ex);
        }
    }
}