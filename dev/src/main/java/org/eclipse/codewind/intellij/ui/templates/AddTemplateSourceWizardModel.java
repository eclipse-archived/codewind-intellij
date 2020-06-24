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

import org.eclipse.codewind.intellij.core.IAuthInfo;
import org.eclipse.codewind.intellij.ui.wizard.BaseCodewindWizardModel;

import java.util.List;

/**
 * Values set in wizard and used by steps
 */
public class AddTemplateSourceWizardModel extends BaseCodewindWizardModel {

    private String urlValue;
    private IAuthInfo authInfo;
    private String nameValue, descriptionValue;
    private List<RepoEntry> repoEntries; // Current list in the repo management table
    private boolean isEdit = false;

    public void setUrlValue(String urlValue) {
        this.urlValue = urlValue;
    }

    public String getTemplateSourceURL() {
        return urlValue;
    }

    public void setAuthInfo(IAuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public IAuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setNameValue(String nameValue) {
        this.nameValue = nameValue;
    }

    public String getNameValue() {
        return nameValue;
    }

    public void setDescriptionValue(String descriptionValue) {
        this.descriptionValue = descriptionValue;
    }

    public String getDescriptionValue() {
        return descriptionValue;
    }

    public void setRepoEntries(List<RepoEntry> repoEntries) {
        this.repoEntries = repoEntries;
    }

    public List<RepoEntry> getRepoEntries() {
        return repoEntries;
    }

    /**
     * Is the wizard for edit or for add
     * @return
     */
    public boolean isEdit() {
        return isEdit;
    }

    /**
     * Set whether the wizard is for edit or add
     * @param isEdit
     */
    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }
}
