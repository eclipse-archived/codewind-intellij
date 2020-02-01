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

import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.InvalidDataException;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.FileUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.eclipse.codewind.intellij.core.cli.ProjectUtil;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.core.connection.ProjectTemplateInfo;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class CodewindModuleBuilder extends JavaModuleBuilder implements ModuleBuilderListener {

    private ProjectTemplateInfo template;

    public CodewindModuleBuilder() {
        addListener(this);
    }

    @Override
    public CodewindModuleType getModuleType() {
        return CodewindModuleType.getInstance();
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        try {
            if (!InstallUtil.getInstallStatus().isInstalled()) {
                return new InstallCodewindStep();
            }
            if (!InstallUtil.getInstallStatus().isStarted()) {
                return new StartCodewindStep();
            }
            return createNewCodewindProjectStep();
        } catch (Exception e) {
            Logger.logWarning(e);
        }
        return null;
    }

    private ModuleWizardStep createNewCodewindProjectStep() throws JSONException, TimeoutException, IOException {
        return new NewCodewindProjectStep(this);
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        try {
            if (!InstallUtil.getInstallStatus().isStarted())
                return new ModuleWizardStep[]{createNewCodewindProjectStep()};
        } catch (Exception e) {
            Logger.logWarning(e);
        }
        return ModuleWizardStep.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
        // Set the module type to Java, because that's really what it is.
        // See super.createModule(moduleModel);
        String moduleFilePath = getModuleFilePath();
        deleteModuleFile(moduleFilePath);
        Module module = moduleModel.newModule(moduleFilePath, StdModuleTypes.JAVA.getId());
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
        String path = getModuleFileDirectory();
        String name = getName();
        String url = template.getUrl();
        String language = template.getLanguage();
        String projectType = template.getProjectType();
        String conid = LocalConnection.CONNECTION_ID;

        Project ideaProject = module.getProject();
        Task.Backgroundable task = new Task.Backgroundable(ideaProject, message("CodewindLabel"), false, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText(message("NewProjectPage_CreateJobLabel", name));
                indicator.setIndeterminate(true);

                try {
                    // Codewind won't create a project in a non-empty directory, and by this
                    // point the IntelliJ project creation framework has already created the .idea
                    // subdirectory inside the project directory, so we have Codewind create
                    // the project in a temp directory and copy it into the project directory

                    Path projectPath = Paths.get(path);
                    Path tmpProjectPath = Files.createTempDirectory("codewind").resolve(projectPath.getFileName());
                    ProjectUtil.createProject(name, tmpProjectPath.toString(), url, conid, new EmptyProgressIndicator());

                    FileUtil.copyDirectory(tmpProjectPath, projectPath);

                    ProjectUtil.bindProject(name, path, language, projectType, conid, new EmptyProgressIndicator());
                    FileUtil.deleteDirectory(tmpProjectPath.getParent().toString(), true);
                } catch (Exception error) {
                    Throwable thrown = Logger.unwrap(error);
                    Logger.logWarning("An error occurred creating project " + name, thrown);
                    CoreUtil.openDialog(CoreUtil.DialogType.ERROR, message("CodewindLabel"), message("StartBuildError", name, thrown.getLocalizedMessage()));
                }

                // Reload the project so the maven importer will run
                ProjectManager.getInstance().reloadProject(ideaProject);
            }
        };
        ProgressManager.getInstance().run(task);
    }
}
