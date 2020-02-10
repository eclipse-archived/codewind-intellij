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

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.wizard.AbstractBindProjectWizardStep;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AddExistingProjectForm {
    private JPanel contentPane;
    private JTextField pathTextField;
    private JButton browseButton;
    private JLabel browseLabel;
    private JPanel rowPanel;
    private JLabel pathLabel;

    private Project project;
    private CodewindConnection connection;
    private AbstractBindProjectWizardStep step;

    public AddExistingProjectForm(AbstractBindProjectWizardStep step, Project project, CodewindConnection connection) {
        this.step = step;
        this.project = project;
        this.connection = connection;
    }

    private void createUIComponents() {
        browseLabel = new JLabel( message("SelectProjectPageDescription"));
        browseButton = new JButton();
        browseButton.setText(message("SelectProjectPageBrowseButton"));
        browseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                FileChooserDescriptor singleFolderDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                singleFolderDescriptor.setTitle(message("SelectProjectPageFilesystemProject"));
                singleFolderDescriptor.setHideIgnored(true);
                singleFolderDescriptor.setShowFileSystemRoots(false);
                @SystemIndependent String basePath = project.getBasePath();
                VirtualFile file = null;
                if (basePath != null) {
                    File projectFolder = new File(basePath);
                    VirtualFile virtualFile = VfsUtil.findFileByIoFile(projectFolder, false);
                    file = FileChooser.chooseFile(singleFolderDescriptor, browseButton.getParent(), project, virtualFile.getParent());
                } else {
                    VirtualFile homeFolder = VfsUtil.getUserHomeDir();
                    file = FileChooser.chooseFile(singleFolderDescriptor, browseButton.getParent(), project, homeFolder);
                }
                if (file != null && file.getPath() != null) {
                    pathTextField.setText(file.getPath());
                }
                if (isProjectAlreadyAdded()) {
                    CoreUtil.openDialog(true, message("BindProjectErrorTitle"), message("BindProjectAlreadyExistsError", project.getName()));
                }
                step.fireStateChanging();
            }
        });
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public String getProjectPath() {
        return pathTextField.getText();
    }

    public void setProjectPath(String path) {
        this.pathTextField.setText(path);
    }

    public boolean isProjectAlreadyAdded() {
        return isCodewindProject(getProjectPath());
    }

    public boolean isCodewindProject(String basePath) {
        CodewindApplication appByLocation = connection.getAppByLocation(basePath);
        return appByLocation != null;
    }
}
