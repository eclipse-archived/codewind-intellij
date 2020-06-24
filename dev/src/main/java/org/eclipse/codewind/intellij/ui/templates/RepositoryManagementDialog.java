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
import org.eclipse.codewind.intellij.core.connection.RepositoryInfo;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;
import org.eclipse.codewind.intellij.ui.form.AbstractCodewindDialogWrapper;
import org.eclipse.codewind.intellij.ui.templates.form.RepositoryManagementForm;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RepositoryManagementDialog extends AbstractCodewindDialogWrapper {

    private RepositoryManagementForm form;
    private List<RepositoryInfo> repoList;
    private CodewindConnection connection;
    private Project project;

    public RepositoryManagementDialog(Project project, CodewindConnection connection, List<RepositoryInfo> repoList) {
        super(project, message("RepoMgmtDialogTitle"), UIConstants.TEMPLATES_INFO_URL);
        this.project = project;
        this.connection = connection;
        this.repoList = repoList;
    }

    /**
     * Init must be called after the dialog has been instantiated
     */
    public void initForm() {
        init();
        form.initForm();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (form == null) {
            form = new RepositoryManagementForm(project, connection, repoList);
        }
        return form.getContentPane();
    }

    @Override
    protected ValidationInfo doValidate() {
        return form.doValidate();
    }

    public void updateRepos() {
        form.updateRepos();
    }
}
