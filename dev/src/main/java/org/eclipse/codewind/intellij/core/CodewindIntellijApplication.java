/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.constants.ProjectCapabilities;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.core.constants.ProjectType;
import org.eclipse.codewind.intellij.core.launch.CoreUiUtil;
import org.jetbrains.annotations.SystemIndependent;

import java.net.MalformedURLException;
import java.nio.file.Path;

public class CodewindIntellijApplication extends CodewindApplication {

    // Debug launch, null if not debugging
    private RunnerAndConfigurationSettings runnerAndConfigurationSettings;

    CodewindIntellijApplication(CodewindConnection connection, String id, String name, ProjectType projectType,
                                ProjectLanguage language, Path localPath) throws MalformedURLException {
        super(connection, id, name, projectType, language, localPath);
    }

    // Debug related methods

    public synchronized void setRunnerAndConfigurationSettings(RunnerAndConfigurationSettings settings) {
        this.runnerAndConfigurationSettings = settings;
    }

    public boolean canInitiateDebugSession() {
        // Only supported for certain languages
        if (projectLanguage.isJava()) {
            // And only if the project supports it
            return supportsDebug();
        }
        return false;
    }

    @Override
    public boolean supportsDebug() {
        // Only supported for certain languages
        if (projectLanguage == ProjectLanguage.LANGUAGE_JAVA || projectLanguage == ProjectLanguage.LANGUAGE_NODEJS) {
            // And only if the project supports it
            ProjectCapabilities capabilities = getProjectCapabilities();
            return (capabilities.supportsDebugMode() || capabilities.supportsDebugNoInitMode()) && capabilities.canRestart();
        }
        return false;
    }

    @Override
    public void clearDebugger() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        Project currentProject = null;
        for (Project project : openProjects) {
            @SystemIndependent String basePath = project.getBasePath();
            if (FileUtil.isSamePath(fullLocalPath.toAbsolutePath().toString(), basePath)) {
                currentProject = project;
                break;
            }
        }
        if (currentProject == null) {
            return;
        }

        // Clear the configuration. Note this can be null but the configuration can still exist, after the IDE restarts up.
        // We should clear it as well as any configurations of the CodewindCofigurationType
        CoreUiUtil.clearRunConfig(currentProject, runnerAndConfigurationSettings);
        runnerAndConfigurationSettings = null;
    }

    @Override
    public void connectDebugger() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        Project currentProject = null;
        for (Project project : openProjects) {
            @SystemIndependent String basePath = project.getBasePath();
            if (FileUtil.isSamePath(fullLocalPath.toAbsolutePath().toString(), basePath)) {
                currentProject = project;
                break;
            }
        }
        if (currentProject == null) {
            return;
        }
        CoreUiUtil.debug(currentProject, this);
    }

    @Override
    public void reconnectDebugger() {
        // Will not automatically connect.  User should select the "Attach Debugger" action
    }
}
