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
package org.eclipse.codewind.intellij.core.launch;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CodewindIntellijApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.ui.debug.CodewindConfigurationType;

import javax.swing.JFrame;
import java.awt.Component;

/**
 * This class contains methods that have UI elements and references but cannot be put in the UI folder because
 * core classes must reference them.  The launch and editor are too interdependent to divide out.
 */
public class CoreUiUtil {

    public static void debug(Project project, CodewindApplication application) {
        String projectName = project.getName();
        RunManager runManager = RunManager.getInstance(project);
        final String debugName = project.getName(); // Name of the debug tab
        RunnerAndConfigurationSettings configurationByTypeAndName = runManager.findConfigurationByTypeAndName(org.eclipse.codewind.intellij.ui.debug.CodewindConfigurationType.ID, debugName);
        if (configurationByTypeAndName == null) {
            configurationByTypeAndName = runManager.createConfiguration(debugName, CodewindConfigurationType.class);
            ConfigurationType configType = configurationByTypeAndName.getType();
            ConfigurationFactory configurationFactory = configType.getConfigurationFactories()[0];
            configurationByTypeAndName = runManager.createConfiguration(debugName, configurationFactory);
            configurationByTypeAndName.setTemporary(true);
        }
        // Set the run config on the application to delete later when the project is switch to run mode (Necessary cast)
        ((CodewindIntellijApplication)application).setRunnerAndConfigurationSettings(configurationByTypeAndName);
        configurationByTypeAndName.storeInLocalWorkspace();
        runManager.addConfiguration(configurationByTypeAndName);
        // Set the current configuration, for the toolbar
        runManager.setSelectedConfiguration(configurationByTypeAndName);
        runManager.setTemporaryConfiguration(configurationByTypeAndName);

        final RunnerAndConfigurationSettings runner = configurationByTypeAndName;
        final RemoteConfiguration remoteConfiguration = (RemoteConfiguration) runner.getConfiguration();
        remoteConfiguration.setAllowRunningInParallel(false);
        // Set the configurations host and port parameters
        remoteConfiguration.PORT = String.valueOf(application.getDebugPort());
        remoteConfiguration.HOST = application.host;
        remoteConfiguration.setModuleName(projectName);

        CoreUtil.invokeLater(() -> {
            runner.setActivateToolWindowBeforeRun(true);
            ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder.createOrNull(DefaultDebugExecutor.getDebugExecutorInstance(), runner);
            if (builder != null) {
                ExecutionManager.getInstance(project).restartRunProfile(builder.build());
            } else {
                try {
                    builder = ExecutionEnvironmentBuilder.create(DefaultDebugExecutor.getDebugExecutorInstance(), runner);
                    ExecutionManager.getInstance(project).restartRunProfile(builder.build());
                } catch (Exception e){
                    Logger.log(e);
                }
            }
            // https://github.com/eclipse/codewind/issues/2531 - Focus switches to previous window after canceling prompt
            // There is no callback or return information from restartRunProfile to tell if dialogs (userApprovesStopForIncompatibleConfigurations) have been canceled.
            // Have to request focus here:
            JFrame frame = WindowManager.getInstance().getFrame(project);
            if (frame != null) {
                Component mostRecentFocusOwner = frame.getMostRecentFocusOwner();
                if (mostRecentFocusOwner != null) {
                    IdeFocusManager.getInstance(project).requestFocus(mostRecentFocusOwner, true);
                }
            }
        });
    }

    public static void clearRunConfig(Project project, RunnerAndConfigurationSettings runnerAndConfigurationSettings) {
        // First, remove the run configuration
        String projectName = project.getName();
        RunManager runManager = RunManager.getInstance(project);
        if (runnerAndConfigurationSettings != null) {
            runManager.removeConfiguration(runnerAndConfigurationSettings);
        }
        RunnerAndConfigurationSettings configurationByTypeAndName = runManager.findConfigurationByTypeAndName(org.eclipse.codewind.intellij.ui.debug.CodewindConfigurationType.ID, projectName);
        if (configurationByTypeAndName != null
                && configurationByTypeAndName.isTemporary()) { // Delete the one that is temporary
            runManager.removeConfiguration(configurationByTypeAndName);
        }
        // Second, terminate the running process (otherwise, when the debug tab is closed, the user will be prompted to terminate)
        ProcessHandler[] runningProcesses = ExecutionManager.getInstance(project).getRunningProcesses();
        for (ProcessHandler process : runningProcesses) {
            if (!process.isProcessTerminated() && !process.isProcessTerminating()) {
                process.detachProcess();
            }
        }

        // Third, close the tool window
        ToolWindowManager toolWindowManager = null;
        if (!project.isDisposed()) {
            toolWindowManager = ToolWindowManager.getInstance(project);
        }
        if (toolWindowManager == null) {
            // It is possible that the project was already disposed of if the IDE was closed
            return; // In this case, it is unnecessary to try to close the debug tool window
        }
        ToolWindow toolWindow = toolWindowManager.getToolWindow(ToolWindowId.DEBUG); // Get the Debug tool window
        if (toolWindow != null) {
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.findContent(project.getName()); // eg. this must be the configuration's debug tab name
            if (content != null) {
                CoreUtil.invokeLater(() -> contentManager.removeContent(content, true));
            }
        }
    }
}