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
package org.eclipse.codewind.intellij.ui.templates.form;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextField;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.form.WidgetUtils;
import org.eclipse.codewind.intellij.ui.form.WizardHeaderForm;
import org.eclipse.codewind.intellij.ui.wizard.BaseCodewindForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class DetailsForm extends BaseCodewindForm {
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel mainContent;
    private JPanel rowPanel;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel descriptionLabel;
    private JTextField descriptionTextField;
    private JPanel rowButtonPanel;
    private JButton resetButton;

    private CodewindConnection connection;
    private String nameValue, descriptionValue;
    private Project project;
    private String[] defaultValues = new String[2];


    public DetailsForm(Project project, CodewindConnection connection, String nameValue, String descriptionValue) {
        this.project = project;
        this.connection = connection;
        this.nameValue = nameValue;
        this.descriptionValue = descriptionValue;
    };

    private void createUIComponents() {
        WizardHeaderForm headerForm = new WizardHeaderForm(message("AddRepoDetailsPage"), message("AddRepoDetailsPageMessage"));
        headerPanel = headerForm.getContentPane();

        nameTextField = new JBTextField();
        descriptionTextField = new JBTextField();

        if (nameValue != null) {
            nameTextField.setText(nameValue);
        }
        if (descriptionValue != null) {
            descriptionTextField.setText(descriptionValue);
        }
        resetButton = new JButton();
        resetButton.setText(message("AddRepoDialogResetButtonLabel"));

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    nameTextField.setText(defaultValues[0] == null ? "" : defaultValues[0]);
                    descriptionTextField.setText(defaultValues[1] == null ? "" : defaultValues[1]);
                    validate(false, resetButton);
                } catch (Exception e) {
                    Logger.logDebug("An error occurred trying to get the template source details", e);
                }

            }
        });

        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                validate(false, nameTextField);
            }
        });

        descriptionTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                validate(false, descriptionTextField);
            }
        });
    }

    public ValidationInfo doValidate() {
        if (nameTextField.getText().length() == 0) {
            return new ValidationInfo(message("AddRepoDialogNoName"));
        } else if (descriptionTextField.getText().length() == 0) {
            return new ValidationInfo(message("AddRepoDialogNoDescription"));
        }
        return null;
    }

    public boolean isComplete() {
        return (nameTextField.getText().length() > 0 && descriptionTextField.getText().length() > 0);
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public String getTemplateSourceName() {
        return WidgetUtils.getTextValue(nameTextField);
    }

    public String getTemplateSourceDescription() {
        return WidgetUtils.getTextValue(descriptionTextField);
    }

    private void validate(boolean init, JComponent component) {
        String errorMsg = null;
        nameValue = WidgetUtils.getTextValue(nameTextField);
        descriptionValue = WidgetUtils.getTextValue(descriptionTextField);
        resetButton.setVisible(defaultValues[0] != null);
        if (resetButton.isVisible()) {
            resetButton.setEnabled((nameValue == null && defaultValues[0] != null) || (nameValue != null && !nameValue.equals(defaultValues[0])) ||
                    (descriptionValue == null && defaultValues[1] != null) || (descriptionValue != null && !descriptionValue.equals(defaultValues[1])));
        }
        if (nameValue == null) {
            errorMsg = message("AddRepoDialogNoName");
        } else if (descriptionValue == null) {
            errorMsg = message("AddRepoDialogNoDescription");
        }
        fireStateChanged(new ChangeEvent(component));
    }

    public void setDefaultValues(String [] defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void setNameValue(String name) {
        this.nameValue = name;
        nameTextField.setText(name);
    }

    public void setDescriptionValue(String description) {
        this.descriptionValue = description;
        descriptionTextField.setText(description);
    }
}
