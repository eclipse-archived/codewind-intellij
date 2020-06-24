/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.ui.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SdkSettingsStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.FileUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.ProjectUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.core.connection.ProjectTemplateInfo;
import org.eclipse.codewind.intellij.ui.tree.CodewindToolWindowHelper;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.eclipse.codewind.intellij.core.constants.IntelliJConstants.IDEA_FOLDER;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class CodewindModuleBuilder extends JavaModuleBuilder implements ModuleBuilderListener {

    private ProjectTemplateInfo template;
    private boolean isSuccessful;
    private CodewindConnection connection;
    private NewCodewindProjectStep newCodewindProjectStep;

    public CodewindModuleBuilder() {
        addListener(this);
        newCodewindProjectStep = new NewCodewindProjectStep(this);
    }

    @Override
    public CodewindModuleType getModuleType() {
        return CodewindModuleType.getInstance();
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new CustomOptionsStep();
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{this.newCodewindProjectStep};
    }

    @NotNull
    @Override
    public com.intellij.openapi.module.Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
        // Set the module type to Java, because that's really what it is.
        // See super.createModule(moduleModel);
        String moduleFilePath = getModuleFilePath();
        deleteModuleFile(moduleFilePath);
        com.intellij.openapi.module.Module module = moduleModel.newModule(moduleFilePath, StdModuleTypes.JAVA.getId());
        setupModule(module);

        return module;
    }

    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep) {
        return new SdkSettingsStep(settingsStep, this, this::isSuitableSdkType);
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        modifiableRootModel.inheritSdk();
        super.setupRootModel(modifiableRootModel);
    }

    public void setSelectedTemplate(ProjectTemplateInfo template) {
        this.template = template;
    }

    @Override
    public void moduleCreated(@NotNull Module module) {
        Project ideaProject = module.getProject();

        // There should be an instance of the Codewind view. Set the initial project to be selected for this window. Also set this right away.
        CodewindToolWindowHelper.setInitialProjectToSelect(module.getProject()); // Set initial selection for the Codewind Tool Window

        // Register post startup activities to run after
        StartupManager.getInstance(ideaProject).registerPostStartupActivity(() -> {
            // Sometimes we see the notification bubbles.  Try to resolve them
            Notification[] notificationsOfType = NotificationsManager.getNotificationsManager().getNotificationsOfType(Notification.class, module.getProject());
            for (Notification n : notificationsOfType) {
                if ("Maven Import".equals(n.getGroupId())) { // We've already imported it, so this notification can be resolved
                    n.expire();
                }
                if ("Maven: non-managed pom.xml".equals(n.getGroupId())) { // Missed doing it? If so, then try to import it again
                    addAndImportProject(module.getProject());
                }
            }
            // Finally, open the tool window (Updates should have already happened)
            CodewindToolWindowHelper.openCodewindWindow(module.getProject()); // Open the Codewind Tool Window
        });
    }

    @Nullable
    @Override
    public Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
        Module module = super.commitModule(project, model);
        // Move Codewind project creation to after the commit so that it is not executed within the write action of the wizard framework
        // Otherwise any dialog (progress monitor) will not appear and the thread will be run in the calling thread, which actually blocks the UI
        // especially in cases where Project Create takes a long time (eg. when downloading images)
        try {
            module = postCommitModule(module);
            if (module != null) {
                // Add and import the project as a maven project programmatically right away
                addAndImportProject(module.getProject());
            }
        } catch (ProcessCanceledException e) {
            Messages.showErrorDialog(message("NewProjectSetupProcessCanceledMessage"), message("NewProjectSetupProcessCanceledTitle")); // Must run now, after cancel is pressed
            // Need to throw this exception, specifically if the process is cancelled. Do not return null
            // since it does not do anything by the framework.
            // This will prevent the prompt to "open the project in a new window" from opening.
            throw e;
        }
        return module;
    }

    private Module postCommitModule(@NotNull Module module) throws ProcessCanceledException {
        String path = getModuleFileDirectory();
        String name = getName();
        String url = template.getUrl();
        String language = template.getLanguage();
        String projectType = template.getProjectType();
        String conid = connection != null ? connection.getConid() : LocalConnection.DEFAULT_ID;
        Project ideaProject = module.getProject();

        // The 'appsody init' command will fail to initialize the maven cache if there is no JDK on the PATH and
        // JAVA_HOME is not set.  We set JAVA_HOME for the process running 'cwctl create' to ensure this doesn't happen.
        Sdk sdk = ProjectRootManager.getInstance(ideaProject).getProjectSdk();
        String javaHome = sdk == null ? null : sdk.getHomePath();
        if (javaHome == null) {
            Logger.log("createProject: no sdk set for project: " + ideaProject.getName());
        }

        SetupCodewindProjectRunnable setupProjectRunnable = new SetupCodewindProjectRunnable(path, name, url, language, projectType, conid, javaHome);
        // This MUST run synchronously, even if we have to wait for the image to download. If there is an issue, the user can cancel
        // True if operation completed successfully, or false if cancelled.
        try {
            // Handle error conditions. The template source repo might not be found or available. IOException (404) possible
            Object rc = ProgressManager.getInstance().runProcessWithProgressSynchronously(setupProjectRunnable, message("NewProjectWizard_ProgressTitle"), true, ideaProject);
            if (rc instanceof Boolean) {
                isSuccessful = ((Boolean) rc).booleanValue();
            }
        } catch (Exception e) {
            if (!(e instanceof ProcessCanceledException)) { // If the user cancelled it, don't log it
                Throwable thrown = Logger.unwrap(e);
                Logger.logWarning("An error occurred creating project " + name, thrown);
            }
            throw e; // rethrow so that a message dialog will appear
        }
        return module;
    }

    private void addAndImportProject(Project project) {
        @SystemIndependent String basePath = project.getBasePath();
        if (basePath != null) {
            File pomXml = new File(basePath.concat(File.separator + "pom.xml"));
            VirtualFile virtualFile = VfsUtil.findFileByIoFile(pomXml, false);
            MavenProjectsManager manager = MavenProjectsManager.getInstance(project);
            if (manager != null && virtualFile != null) {
                manager.addManagedFilesOrUnignore(Collections.singletonList(virtualFile));
                manager.scheduleImportAndResolve();
            }
        }
    }

    void onWizardFinished() throws CommitStepException {
        // This method will get called even if the IntelliJ project being created is not a
        // Codewind project.  In that case a call to getName() will return nulll.
        if (getName() == null)
            return;

        // Check that the project folder is empty except for the '.idea' folder
        File projectFolder = new File(getModuleFileDirectory());
        File[] files = projectFolder.listFiles();
        if (files.length > 1 || (files.length == 1 && !files[0].getName().equals(IDEA_FOLDER)))
            throw new CommitStepException(message("ProjectFolderNotEmpty", projectFolder));

        // Check if there is already a Codewind project with the given name
        List<CodewindApplication> applications = ConnectionManager.getManager().getLocalConnection().getApps();
        if (applications.stream().anyMatch(a -> a.getName().equals(getName())))
            throw new CommitStepException(message("NewProjectPage_ProjectExistsError", getName()));
    }

    @Override
    public void cleanup() {
        super.cleanup();
        // If the Codewind project set up was cancelled, simply remove the folder
        if (!isSuccessful) {
            String moduleFileDirectory = getModuleFileDirectory(); // The folder that contains the module file
            Logger.log("Cancel was pressed. Removing folder " + moduleFileDirectory);
            try {
                FileUtil.deleteDirectory(moduleFileDirectory, true);
            } catch (IOException e) {
                Logger.log(e);
            }
        }
    }

    /**
     * This should be called before the wizard is shown if launched from the context menu (NewCodewindProjectAction)
     * Or, if necessary, called from the connections page
     *
     * @param connection
     */
    public void setConnection(CodewindConnection connection) {
        this.connection = connection;
        // Update all necessary steps with this connection
        newCodewindProjectStep.setConnection(connection);
    }
}