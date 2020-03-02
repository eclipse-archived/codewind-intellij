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
package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.tabs.TabbedContentAction;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.core.console.ProjectLogInfo;
import org.eclipse.codewind.intellij.core.console.SocketConsole;
import org.eclipse.codewind.intellij.ui.CodewindToolWindow;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;
import org.eclipse.codewind.intellij.ui.toolwindow.UpdateHandler;
import org.eclipse.codewind.intellij.ui.tree.CodewindToolWindowHelper;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ShowAllLogFilesTask extends Task.Backgroundable {

    private final CodewindApplication application;
    private ToolWindow logFilesToolWindow;
    private ContentManager contentManager = null;

    public ShowAllLogFilesTask(CodewindApplication application, Project project) {
        super(project, message("ShowAllLogFilesAction"));
        this.application = application;
    }
    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        CoreUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                initToolWindow();

                for (ProjectLogInfo logInfo : application.getLogInfos()) {
                    Content content = getContentForProjectLogInfo(logInfo);
                    SocketConsole socketConsole = null;
                    if (content == null) {
                        socketConsole = createLogFileToolWindow(logInfo);
                    }
                    try {
                        if (socketConsole != null) {
                            socketConsole.initialize();
                        }
                    } catch( Exception e) {
                        Logger.log(e);
                    }
                }
                if (!logFilesToolWindow.isVisible() && logFilesToolWindow.isAvailable()) {
                    logFilesToolWindow.show(null);
                    logFilesToolWindow.activate(null, true, true);
                }
            }
        });
    }

    private void initToolWindow() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        logFilesToolWindow = toolWindowManager.getToolWindow(CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID);
        boolean windowRequiresRegistration = false;
        if (logFilesToolWindow == null) {
            windowRequiresRegistration = true;
            logFilesToolWindow = toolWindowManager.registerToolWindow(CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID, true, ToolWindowAnchor.BOTTOM);
            logFilesToolWindow.setContentUiType(ToolWindowContentUiType.COMBO, null);
            logFilesToolWindow.setIcon(IconCache.getCachedIcon(IconCache.ICONS_CODEWIND_13PX_SVG));
        }
        contentManager = logFilesToolWindow.getContentManager();
        if (windowRequiresRegistration) {
            contentManager.addContentManagerListener(new ContentManagerAdapter() {
                public void contentRemoved(@NotNull ContentManagerEvent event) {
                    ContentManagerEvent.ContentOperation operation = event.getOperation();
                    if (operation.equals(ContentManagerEvent.ContentOperation.remove)) {

                        String connectionId = application.connection.getConid();
                        String projectId = application.projectID;
                        if (connectionId != null && projectId != null) {
                            CodewindToolWindow.getToolWindowUpdateHandler().removeAppUpdateListener(event.getContent(), connectionId, projectId);
                        }

                        Key<SocketConsole> key = CodewindToolWindow.getLogsKeyMap().get(event.getContent());
                        SocketConsole sc = getProject().getUserData(key);
                        CoreUtil.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (sc != null) {
                                    sc.dispose(); // Need to disconnect
                                }
                            }
                        });
                    }
                }

                public void contentRemoveQuery(@NotNull ContentManagerEvent event) {
                    // Empty
                }
            });
        }
    }

    private Content getContentForProjectLogInfo(ProjectLogInfo logInfo) {
        Content content = contentManager.findContent(org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message("LogFileConsoleName" ,application.name, logInfo.logName));
        return content;
    }

    private SocketConsole createLogFileToolWindow(ProjectLogInfo logInfo) {
        Content content = null;
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(getProject()).getConsole();

        SimpleToolWindowPanel simplePanel = new SimpleToolWindowPanel(false, true);
        content = ContentFactory.SERVICE.getInstance().createContent(simplePanel, org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message("LogFileConsoleName", application.name, logInfo.logName), true);
        simplePanel.setContent(consoleView.getComponent());
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        content.setIcon(IconCache.getCachedIcon(IconCache.ICONS_CODEWIND_13PX_SVG));
        content.setDescription(org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message("LogFileConsoleName", application.name, logInfo.logName));
        contentManager.addContent(content);
        Disposer.register(getProject(), consoleView);
        
        SocketConsole socketConsole = new SocketConsole(content, consoleView, content.getDisplayName(), logInfo, application);
        DefaultActionGroup toolbarGroup = new DefaultActionGroup();

        AnAction[] consoleActions = consoleView.createConsoleActions();
        for (int i = 0; i < consoleActions.length; i++) {
            if (!(consoleActions[i] instanceof PreviousOccurenceToolbarAction ||
                    consoleActions[i] instanceof NextOccurenceToolbarAction)) {
                toolbarGroup.add(consoleActions[i]);
            }
        }
        final AnAction helpAction = new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                try {
                    BrowserLauncher.getInstance().browse(new URI(UIConstants.GETTING_STARTED_INFO_URL));
                } catch (URISyntaxException ex) {
                    Logger.log(ex);
                }
            }
        };
        helpAction.getTemplatePresentation().setIcon(AllIcons.Actions.Help);
        toolbarGroup.add(helpAction);
        final AnAction closeTabAction = new TabbedContentAction.CloseAction(content) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                super.actionPerformed(e);
                socketConsole.dispose();
            }
        };
        closeTabAction.getTemplatePresentation().setIcon(AllIcons.Actions.Cancel);
        toolbarGroup.add(closeTabAction);

        Key<SocketConsole> key = new Key<>(content.getDisplayName());
        key.set(getProject(), socketConsole);
        getProject().putUserData(key, socketConsole);
        CodewindToolWindow.getLogsKeyMap().put(content, key);
        simplePanel.setToolbar(ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, toolbarGroup, false).getComponent());


        String connectionId = application.connection.getConid();
        if (connectionId == null) {
            connectionId = LocalConnection.DEFAULT_ID;
        }
        final Content finalContent = content;
        CodewindToolWindow.getToolWindowUpdateHandler().addAppUpdateListener(content, connectionId, application.projectID, new UpdateHandler.AppUpdateListener() {
            @Override
            public void update() {
                // Empty - taken care of by original SocketConsole code
                // Todo possibly move SocketConsole to UpdateHandler
                // This is where we should be printing to the consoleView
            }

            @Override
            public void remove() {
                ToolWindowManager toolWindowManager = null;
                if (!getProject().isDisposed()) {
                    toolWindowManager = ToolWindowManager.getInstance(getProject());
                }
                if (toolWindowManager == null) {
                    // It is possible that the project was already disposed of if the IDE was closed
                    return; // In this case, it is unnecessary to try to close the log windows
                }
                ToolWindow logToolWindow = toolWindowManager.getToolWindow(CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID);
                if (logToolWindow != null) {
                    ContentManager contentManager = logToolWindow.getContentManager();
                    Content content = contentManager.findContent(org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message("LogFileConsoleName", application.name, logInfo.logName));
                    CoreUtil.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (finalContent == content && content != null) {  // null check here in case it gets cleared
                                contentManager.removeContent(content, true);
                            }
                        }
                    });
                }
            }
        });
        return socketConsole;
    }
}
