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

package org.eclipse.codewind.intellij.module;

import org.eclipse.codewind.intellij.core.connection.ProjectTemplateInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.core.constants.ProjectType;
import org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TemplateTableModel extends AbstractTableModel {

    private ProjectTemplateInfo[] templates = {};

    public void update(List<ProjectTemplateInfo> templates) {
        this.templates = templates.toArray(new ProjectTemplateInfo[0]);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return templates.length;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return CodewindUIBundle.message("NewProjectPage_TemplateColumn");
            case 1:
                return CodewindUIBundle.message("NewProjectPage_TypeColumn");
            case 2:
                return CodewindUIBundle.message("NewProjectPage_LanguageColumn");
            default:
                throw new IllegalStateException("Unexpected value: " + column);
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return templates[row].getLabel();
            case 1:
                return ProjectType.getDisplayName(templates[row].getProjectType());
            case 2:
                return ProjectLanguage.getDisplayName(templates[row].getLanguage());
            default:
                throw new IllegalStateException("Unexpected value: " + column);
        }
    }

    public ProjectTemplateInfo getTemplateAt(int row) {
        return templates[row];
    }
}
