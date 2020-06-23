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

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.PathUtil;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.ProjectUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.constants.ProjectInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.core.constants.ProjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ConfirmProjectTypeStep extends AbstractBindProjectWizardStep {
    public static String STEP_ID = "ConfirmProjectTypeStep";
    private Project project;
    private CodewindConnection connection;
    private String projectPath;
    private JPanel contentPanel;
    private JTextArea descriptionLabel;
    private JTextField projectPathField;
    private JTextField typeField;
    private JTextField languageField;
    private ProjectInfo initialProjectType;
    private boolean isAcceptableLanguage = false;  // Java or Unknown are acceptable (Liberty Docker java is unknown)

    public ConfirmProjectTypeStep(@Nullable String title, Project project, CodewindConnection connection) {
        super(title);
        this.project = project;
        this.connection = connection;
    }

    @NotNull
    @Override
    public Object getStepId() {
        return STEP_ID;
    }

    @Nullable
    @Override
    public Object getNextStepId() {
        return ProjectTypeSelectionStep.STEP_ID;
    }

    @Nullable
    @Override
    public Object getPreviousStepId() {
        return ProjectFolderStep.STEP_ID;
    }

    @Override
    public boolean isComplete() {
        if (typeField != null && typeField.getText().isEmpty()) {
            return false;
        }
        if (projectPath == null) {
            return false;
        }
        if (initialProjectType != null && initialProjectType.language != null) {
            // If not Java or not Unknown, then don't allow the wizard to finish
            String languageId = initialProjectType.language.getId();
            if (languageId != null) {
                isAcceptableLanguage = languageId.equals(ProjectLanguage.LANGUAGE_JAVA.getId()) || languageId.equals(ProjectLanguage.LANGUAGE_UNKNOWN.getId());
                if (!this.isAcceptableLanguage) {
                    return false;
                }
            }
        }
        // All other cases, allow the user to proceed.
        return true;
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
        // Empty. Use onStepLeaving instead.
    }

    /*
     * Originally created this without a GUI form to see the extra code that needed to be added.
     * eg. setLabelFor(), layout details, constraint details, etc. are all unnecessary and would be captured instead in the GUI form/XML.
     */
    @Override
    public JComponent getComponent() {
        // We don't want to recreate the widgets again.  getComponent gets called multiple times by the wizard framework
        if (contentPanel != null) {
            return contentPanel;
        }
        contentPanel = new JPanel();

        GridBagLayout gl = new GridBagLayout();
        contentPanel.setLayout(gl);
        contentPanel.setAlignmentY(1.0f);

        descriptionLabel = new JTextArea(message("ProjectValidationPageMsg", ""));
        descriptionLabel.setWrapStyleWord(true);
        descriptionLabel.setLineWrap(true);
        descriptionLabel.setEditable(false);
        descriptionLabel.setOpaque(false);

        contentPanel.add(descriptionLabel, new GridBagConstraints(0, 0, 1, 1, 0.5, 0, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1, false, false));
        contentPanel.add(formPanel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        JLabel projectPathLabel = new JLabel(message( "SelectProjectPagePathLabel"));
        formPanel.add(projectPathLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        projectPathField = new JTextField();
        projectPathField.setEditable(false);
        projectPathField.setOpaque(false);
        formPanel.add(projectPathField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        projectPathLabel.setLabelFor(projectPathField);

        JLabel typeLabel = new JLabel(message( "ProjectValidationPageTypeLabel"));
        formPanel.add(typeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));

        typeField = new JTextField();
        typeField.setOpaque(false);
        formPanel.add(typeField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));

        JLabel languageLabel = new JLabel(message( "ProjectValidationPageLanguageLabel"));
        formPanel.add(languageLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));

        languageField = new JTextField();
        languageField.setOpaque(false);
        formPanel.add(languageField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));

        JPanel verticalSpacer = new JPanel();
        contentPanel.add(verticalSpacer, new GridBagConstraints(0, 2, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        // Set the label to be the same font as a text field
        descriptionLabel.setFont(typeField.getFont());
        return contentPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @NotNull
    public void setProjectPath(String path) {
        this.projectPath = path;
        if (this.projectPathField != null) {
            projectPathField.setText(projectPath);
        }
        String name = PathUtil.getFileName(projectPath);
        descriptionLabel.setText(message("ProjectValidationPageMsg", name));
    }

    public ProjectInfo getInitialProjectInfo() {
        return this.initialProjectType;
    }

    public void validateProject() {
        String name = PathUtil.getFileName(projectPath);
        // Todo: This should be cancellable (ProjectUtil.projectValidate's Process)
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                    initialProjectType = ProjectUtil.validateProject(name, projectPath, null, connection.getConid(), indicator);
                    if (initialProjectType != null) {
                        if (initialProjectType.language != null) {
                            languageField.setText(initialProjectType.language.getDisplayName());
                        }
                        if (initialProjectType.type != null) {
                            typeField.setText(initialProjectType.type.getDisplayName());
                        }
                    }
                } catch (Exception e) {
                    // Fill this in and ensure user can go to the last page
                    Logger.log(e);
                    typeField.setText(ProjectType.TYPE_UNKNOWN.getDisplayName());
                    languageField.setText(ProjectLanguage.LANGUAGE_UNKNOWN.getDisplayName());
                }
            }
        }, message("ProjectValidationAnalyzingProject"), false, this.project);
    }

    @Override
    protected void onStepEntering(BindProjectModel model) {
        boolean projectInfoChanged = true;
        String projectPath = model.getProjectPath();
        if (projectPath != null && projectPath.equals(this.projectPath)) {
            projectInfoChanged = false;
        }
        setProjectPath(model.getProjectPath());
        if (projectInfoChanged) {
            validateProject();
        }
    }

    @Override
    protected void onStepLeaving(BindProjectModel model) {
        model.setProjectInfo(getInitialProjectInfo());
    }

    @Override
    protected void postDoNextStep(BindProjectModel model) {
        String name = PathUtil.getFileName(projectPath);
        if (initialProjectType != null && initialProjectType.language != null) {
            String languageId = initialProjectType.language.getId();
            isAcceptableLanguage = languageId.equals(ProjectLanguage.LANGUAGE_JAVA.getId()) || languageId.equals(ProjectLanguage.LANGUAGE_UNKNOWN.getId());
            if (!isAcceptableLanguage) {
                CoreUtil.openDialog(true, message("ProjectValidationPageTitle"), message("ProjectValidationPageNotJavaMsg", name));
            }
        } else {
            CoreUtil.openDialog(true, message("ProjectValidationPageTitle"), message("ProjectValidationPageFailMsg"));
        }
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        // ignore
    }

    @Override
    public void addListener(ChangeListener listener) {
        // ignore
    }
}
