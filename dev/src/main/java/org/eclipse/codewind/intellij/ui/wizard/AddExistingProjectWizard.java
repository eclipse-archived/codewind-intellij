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

import com.intellij.ide.wizard.AbstractWizardEx;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.PathUtil;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.ProjectUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ProjectTypeInfo;
import org.eclipse.codewind.intellij.core.connection.ProjectTypeInfo.ProjectSubtypeInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.ui.tree.CodewindToolWindowHelper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.JFrame;
import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AddExistingProjectWizard extends AbstractWizardEx {

    private BindProjectModel model;
    private CodewindConnection connection;
    private Project intellijProject;
    private boolean doBind = true;
    private Exception validationException = null;
    private Exception bindException = null;

    public AddExistingProjectWizard(String title, @Nullable Project project, List<AbstractBindProjectWizardStep> steps, @Nullable CodewindConnection connection) {
        super(title, project, steps);
        this.connection = connection;
        this.intellijProject = project;
        model = new BindProjectModel();
    }

    @Override
    protected boolean canFinish() {
        return super.canFinish();
    }

    @Override
    protected void doNextAction() {
        AbstractBindProjectWizardStep currentStepObject = (AbstractBindProjectWizardStep) getCurrentStepObject();
        currentStepObject.onStepLeaving(model);
        // Get next step object reference for use after super.doNextAction();
        AbstractBindProjectWizardStep nextStepObject = (AbstractBindProjectWizardStep) getNextStepObject();
        nextStepObject.onStepEntering(model); // Do API calls prior to entering the page
        // Now, switch pages and show content
        super.doNextAction();
        // getNextStepObject() will return the next step, which isn't what we want
        nextStepObject.postDoNextStep(model); // This could be for validation right after the page appears
    }

    @Override
    protected void doPreviousAction() {
        // Thus far, only care about stepLeaving
        AbstractBindProjectWizardStep currentStepObject = (AbstractBindProjectWizardStep) getCurrentStepObject();
        currentStepObject.onStepLeaving(model);
        super.doPreviousAction();
    }

    @Override
    protected void doOKAction() {
        AbstractBindProjectWizardStep currentStepObject = (AbstractBindProjectWizardStep) getCurrentStepObject();
        currentStepObject.onStepLeaving(model);
        final String name = PathUtil.getFileName(model.getProjectPath());
        doBind = true;

//  Todo: Perform selected action if project already bound to another connection. For local, ignore

//        final List<CodewindApplication> existingDeployments = new ArrayList<>();
//        for (CodewindConnection conn : ConnectionManager.getManager().activeConnections()) {
//            if (conn.isConnected()) {
//                CodewindApplication app = conn.getAppByLocation((model.getProjectPath()));
//                if (app != null && app.isEnabled()) {
//                    existingDeployments.add(app);
//                }
//            }
//        }

        // Use the detected type if the validation page is active otherwise use the type from the project type page
        final ProjectInfo projectInfo = model.getProjectInfo();
        final ProjectTypeInfo typeInfo = model.getProjectTypeInfo();
        // Todo: for remote, check for a push registry if Codewind style project.  See Codewind for Eclipse

        // Bind the project to the connection
        String path = model.getProjectPath();
        bindException = null;
        if (projectInfo != null && projectInfo.type.getId().equals(typeInfo.getId())) {
            continueWithBind(name, path, projectInfo.language.getId(), typeInfo);
        } else { // If the user chooses some other type instead of the detected type
            final ProjectSubtypeInfo subtypeInfo = model.getSubtypeInfo();
            // call validate again with type and subtype hint
            // allows it to run extension commands if defined for that type and subtype
            validationException = null;
            String language = null;
            if (projectInfo != null) {
                language = projectInfo.language.getId();  // First use detected language
            }
            if (subtypeInfo != null) {
                language = subtypeInfo.id; // If user overrides it, use it instead
            }
            if (language == null) {
                language = ProjectLanguage.LANGUAGE_JAVA.getId(); // if still null, use java
            }
            // Todo - make all of these processes in this wizard cancellable
            final String finalLanguage = language;
            ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                        ProjectUtil.validateProject(name, path, typeInfo.getId() + ":" + finalLanguage, connection.getConid(), indicator);
                    } catch (Exception e) {
                        Logger.log(e);
                        validationException = e;
                    }
                }
            }, message("ProjectValidationTask", name), false, intellijProject);

            if (validationException != null) {
                CoreUtil.invokeLater(() -> {
                    int rc1 = CoreUtil.showYesNoDialog(message("ProjectValidationTitle"), message("ProjectValidationFailedContinueProjectBind", validationException.getMessage()));
                    if (rc1 == Messages.YES) {
                        continueWithBind(name, path, finalLanguage, typeInfo);
                    } else {
                        doBind = false;
                    }
                });
            }

            if (doBind && validationException == null) {
                continueWithBind(name, path, finalLanguage, typeInfo);
            }
        }

    }

    private void continueWithBind(String name, String path, String language, ProjectTypeInfo typeInfo) {
        try {
            // Todo - make all of these processes in this wizard cancellable
            ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                        ProjectUtil.bindProject(name, path, language, typeInfo.getId(), connection.getConid(), indicator);
                    } catch (Exception e) {
                        Logger.log(e);
                        bindException = e;
                    }
                }
            }, message("BindProjectWizardJobLabel", name), true, intellijProject);
        } catch (Exception e) {
            Logger.log(e);
        }

        // Dismiss the wizard regardless of project bind error
        super.doOKAction();

        // If the project bind failed, show an error dialog and don't prompt to open the project
        if (bindException != null) {
            CoreUtil.openDialog(true, message("BindProjectWizardTitle"), message("BindProjectWizardError", name));
            return;
        }

        // Proceed with opening the project
        try {
            String selectedProjectPath = model.getProjectPath();
            // Check if the project is already opened in a window
            Project[] openedProjects = ProjectManager.getInstance().getOpenProjects();
            for (int i = 0; i < openedProjects.length; i++) {
                Project openedProject = openedProjects[i];
                @SystemIndependent String basePath = openedProject.getBasePath();
                if (selectedProjectPath.equals(basePath)) {
                    // If the project is already opened, just set it active
                    JFrame frame = WindowManager.getInstance().getFrame(openedProject);
                    if (frame != null) {
                        frame.setVisible(true);
                    }
                    return;
                }
            }
            CoreUtil.invokeLater(() -> {
                Project targetProject = com.intellij.ide.impl.ProjectUtil.openOrImport(model.getProjectPath(), intellijProject, false);
                if (targetProject != null) {
                    CodewindToolWindowHelper.openWindow(targetProject); // Open the Codewind Tool Window
                }
            });
        } catch (Exception ex) {
            Logger.log(ex);
        }
    }

    @Nullable
    @Override
    protected String getHelpID() {
        return null;
    }
}
