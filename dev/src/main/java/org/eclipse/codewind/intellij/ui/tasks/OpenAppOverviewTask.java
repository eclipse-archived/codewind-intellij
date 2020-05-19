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

import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.*;
import com.intellij.ui.content.tabs.TabbedContentAction;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.ui.CodewindToolWindow;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.eclipse.codewind.intellij.ui.actions.RefreshAction;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;
import org.eclipse.codewind.intellij.ui.form.AppOverviewFrame;
import org.eclipse.codewind.intellij.ui.toolwindow.UpdateHandler;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class OpenAppOverviewTask extends Task.Backgroundable {

    public final static String PROJECT_OVERVIEW_TOOLWINDOW_ID = "Codewind Project Overview";
    private final CodewindApplication application;

    public OpenAppOverviewTask(CodewindApplication application, Project project) {
        super(project, message("ACTION_OPEN_APP_OVERVIEW"));
        this.application = application;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        CoreUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolWindow overviewToolWindow = toolWindowManager.getToolWindow(PROJECT_OVERVIEW_TOOLWINDOW_ID);
                Content content = null;
                ContentManager contentManager = null;
                if (overviewToolWindow == null) {
                    overviewToolWindow = toolWindowManager.registerToolWindow(PROJECT_OVERVIEW_TOOLWINDOW_ID, true, ToolWindowAnchor.BOTTOM);
                    overviewToolWindow.setContentUiType(ToolWindowContentUiType.TABBED, null);
                    overviewToolWindow.setIcon(IconCache.getCachedIcon(IconCache.ICONS_CODEWIND_13PX_SVG));
                    contentManager = overviewToolWindow.getContentManager();
                    contentManager.addContentManagerListener(new ContentManagerListener() {
                        // Manually closing the overview page.  Remove the UpdateHandler listener
                        public void contentRemoved(@NotNull ContentManagerEvent event) {
                            ContentManagerEvent.ContentOperation operation = event.getOperation();
                            Content content = event.getContent();
                            if (operation.equals(ContentManagerEvent.ContentOperation.remove)) {
                                String connectionId = application.connection.getConid();
                                String projectId = application.projectID;
                                if (connectionId != null && projectId != null) {
                                    CodewindToolWindow.getToolWindowUpdateHandler().removeAppUpdateListener(content, connectionId, projectId);
                                }
                            }
                        }
                    });

                } else { // tool window created already but the project overview was not added
                    contentManager = overviewToolWindow.getContentManager();
                    content = contentManager.findContent(application.name);
                }
                if (content == null) {
                    AppOverviewFrame form = new AppOverviewFrame(application, getProject());
                    SimpleToolWindowPanel simplePanel = new SimpleToolWindowPanel(false, true);
                    content = ContentFactory.SERVICE.getInstance().createContent(simplePanel, application.name, true);
                    simplePanel.setContent(form.overviewPanel);
                    content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
                    content.setIcon(IconCache.getCachedIcon(IconCache.ICONS_CODEWIND_13PX_SVG));
                    content.setDescription(message("AppOverviewOfProject", application.name));
                    contentManager.addContent(content);

                    DefaultActionGroup toolbarGroup = new DefaultActionGroup();
                    final AnAction refreshTabAction = new RefreshAction() {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            CoreUtil.runAsync(new Runnable() { // Do NOT block the UI
                                @Override
                                public void run() {
                                    application.connection.refreshApps(application.projectID);
                                    CoreUtil.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            form.update(application);
                                        }
                                    });
                                }
                            });
                        }
                    };
                    refreshTabAction.getTemplatePresentation().setIcon(AllIcons.Actions.Refresh);
                    toolbarGroup.add(refreshTabAction);
                    final AnAction helpAction = new AnAction() {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            try {
                                BrowserLauncher.getInstance().browse(new URI(UIConstants.GETTING_STARTED_INFO_URL));
                            } catch (URISyntaxException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    helpAction.getTemplatePresentation().setIcon(AllIcons.Actions.Help);
                    toolbarGroup.add(helpAction);
                    final AnAction closeTabAction = new TabbedContentAction.CloseAction(content);
                    closeTabAction.getTemplatePresentation().setIcon(AllIcons.Actions.Cancel);
                    toolbarGroup.add(closeTabAction);
                    simplePanel.setToolbar(ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, toolbarGroup, false).getComponent());

                    String connectionId = application.connection.getConid();
                    if (connectionId == null) {
                        connectionId = LocalConnection.DEFAULT_ID;
                    }
                    final Content finalContent = content;
                    CodewindToolWindow.getToolWindowUpdateHandler().addAppUpdateListener(content, connectionId, application.projectID, new UpdateHandler.AppUpdateListener() {
                        @Override
                        public void update() {
                            final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
                            ToolWindow overviewToolWindow = toolWindowManager.getToolWindow(OpenAppOverviewTask.PROJECT_OVERVIEW_TOOLWINDOW_ID);
                            if (overviewToolWindow != null) {
                                ContentManager contentManager = overviewToolWindow.getContentManager();
                                Content content = contentManager.findContent(application.name);
                                if (content != null) {
                                    form.update(application);
                                }
                            }
                        }

                        @Override
                        public void remove() {
                            ToolWindowManager toolWindowManager = null;
                            if (!getProject().isDisposed()) {
                                toolWindowManager = ToolWindowManager.getInstance(getProject());
                            }
                            if (toolWindowManager == null) {
                                // It is possible that the project was already disposed of if the IDE was closed
                                return; // In this case, it is unnecessary to try to close the overview tool window
                            }
                            ToolWindow overviewToolWindow = toolWindowManager.getToolWindow(OpenAppOverviewTask.PROJECT_OVERVIEW_TOOLWINDOW_ID);
                            if (overviewToolWindow != null) {
                                ContentManager contentManager = overviewToolWindow.getContentManager();
                                Content content = contentManager.findContent(application.name);
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
                }
                overviewToolWindow.show(null);
                overviewToolWindow.activate(null, true, true);
                if (!contentManager.isSelected(content)) {
                    contentManager.setSelectedContent(content);
                }
            }
        });
    }
}
