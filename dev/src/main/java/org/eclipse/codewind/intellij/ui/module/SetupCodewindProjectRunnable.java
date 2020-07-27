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
package org.eclipse.codewind.intellij.ui.module;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.FileUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.ProjectUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class SetupCodewindProjectRunnable implements Runnable {

    private String path;
    private String name;
    private String url;
    private String language;
    private String projectType;
    private String conId;
    private String javaHome;

    public SetupCodewindProjectRunnable(
            String path,
            String name,
            String url,
            String language,
            String projectType,
            String conId,
            String javaHome) {

        this.path = path;
        this.name = name;
        this.url = url;
        this.language = language;
        this.projectType = projectType;
        this.conId = conId;
        this.javaHome = javaHome;
    };

    @Override
    public void run() throws ProcessCanceledException{
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        try {
            progressIndicator.setIndeterminate(false);
            progressIndicator.setText2(message("PleaseWaitForBuild"));
            Path projectPath = Paths.get(path);
            progressIndicator.checkCanceled();

            Path tmpProjectPath = Files.createTempDirectory("codewind").resolve(projectPath.getFileName());
            progressIndicator.checkCanceled();
            Logger.log("Temp project path is " + tmpProjectPath.toAbsolutePath());
            progressIndicator.setText(message("SettingUpProject"));
            ProjectUtil.createProject(name, tmpProjectPath.toString(), url, conId, javaHome, progressIndicator);
            progressIndicator.checkCanceled();
            FileUtil.copyDirectory(tmpProjectPath, projectPath);
            progressIndicator.checkCanceled();

            progressIndicator.setText("Binding project");
            ProjectUtil.bindProject(name, path, language, projectType, conId, progressIndicator);
            progressIndicator.checkCanceled();

            FileUtil.deleteDirectory(tmpProjectPath.getParent().toString(), true);
            progressIndicator.checkCanceled();

            progressIndicator.stop();
        } catch (IOException ioe) {
            if (ioe.getMessage().contains("Directory cannot be removed")) {
                Logger.logWarning("The temporary directory cannot be removed. Clean up the folder manually.", ioe);
            }
        } catch (Exception error) {
            if (!(error instanceof ProcessCanceledException)) {
                Throwable thrown = Logger.unwrap(error);
                Logger.logWarning("An error occurred creating project " + name, thrown);
                CoreUtil.openDialog(CoreUtil.DialogType.ERROR,  message("NewProjectPage_ProjectCreateErrorTitle"),  message("StartBuildError", name, thrown.getLocalizedMessage()));
            }
        }
    }
}
