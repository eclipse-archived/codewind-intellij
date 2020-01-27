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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.TemplateUtil;
import org.eclipse.codewind.intellij.core.connection.ProjectTemplateInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class NewCodewindProjectStep extends ModuleWizardStep {

    private final CodewindModuleBuilder builder;

    private final JPanel panel;
    private final JTable table;

    public NewCodewindProjectStep(CodewindModuleBuilder builder) {
        this.builder = builder;
        this.panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel("Create a Codewind Project");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);

        table = new JBTable(new TemplateTableModel());

        JScrollPane scroll = new JBScrollPane(table);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scroll);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void updateDataModel() {
        builder.setSelectedTemplate(getTableModel().getTemplateAt(table.getSelectedRow()));
    }

    @Override
    public void updateStep() {
        try {
            List<ProjectTemplateInfo> templates = TemplateUtil.listTemplates(true, "local", new EmptyProgressIndicator())
                    .stream()
                    .filter(info -> info.getLanguage().equals("java"))
                    .collect(Collectors.toList());
            getTableModel().update(templates);
        } catch (Exception e) {
            Logger.logWarning(e);
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return table.getSelectedRow() >= 0;
    }

    private TemplateTableModel getTableModel() {
        return (TemplateTableModel) table.getModel();
    }
}
