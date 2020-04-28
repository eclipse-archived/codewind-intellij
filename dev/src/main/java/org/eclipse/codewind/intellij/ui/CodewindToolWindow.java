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

package org.eclipse.codewind.intellij.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.*;
import org.eclipse.codewind.intellij.core.cli.InstallStatus;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionEnv;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
import org.eclipse.codewind.intellij.core.console.SocketConsole;
import org.eclipse.codewind.intellij.ui.actions.*;
import org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle;
import org.eclipse.codewind.intellij.ui.toolwindow.UpdateHandler;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeNodeCellRenderer;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CodewindToolWindow extends JBPanel<CodewindToolWindow> {

    public static String ID = "Codewind";
    public static String DISPLAY_NAME = "";
    private Tree tree;

    private final AnAction installCodewindAction;
    private final AnAction updateCodewindAction;
    private final AnAction uninstallCodewindAction;
    private final AnAction startCodewindAction;
    private final AnAction stopCodewindAction;

    private final AnAction openApplicationAction;
    private final AnAction openAppOverviewAction;
    private final AnAction openIdeaProjectAction;
    private final AnAction startBuildAction;
    private final AnAction enableAutoBuildAction;
    private final AnAction disableAutoBuildAction;
    private final AnAction refreshAction;
    private final AnAction openAppMonitorAction;
    private final AnAction enableInjectMetricsAction;
    private final AnAction disableInjectMetricsAction;
    private final AnAction openPerformanceDashboardAction;
    private final AnAction openTektonDashboardAction;
    private final AnAction enableProjectAction;
    private final AnAction disableProjectAction;
    private final AnAction removeProjectAction;
    private final AnAction addExistingProjectAction;
    private final AnAction showAllLogFilesAction;
    private final AnAction closeAllLogFilesAction;
    private final AnAction restartRunModeAction;
    private final AnAction restartDebugModeAction;
    private final AnAction attachDebuggerAction;
    private final AnAction openShellAction;

    private final AnAction newProjectAction;

    private Project initialSelectedProject;

    public CodewindToolWindow() {
        tree = new Tree();
        tree.setCellRenderer(new CodewindTreeNodeCellRenderer());

        installCodewindAction = new InstallCodewindAction(this::expandLocalTree);
        updateCodewindAction = new UpdateCodewindAction(this::expandLocalTree);
        uninstallCodewindAction = new UninstallCodewindAction(this::expandLocalTree);
        startCodewindAction = new StartCodewindAction(this::expandLocalTree);
        stopCodewindAction = new StopCodewindAction(this::expandLocalTree);

        openApplicationAction = new OpenApplicationAction();
        openAppOverviewAction = new OpenAppOverviewAction();
        openIdeaProjectAction = new OpenIdeaProjectAction();
        startBuildAction = new StartBuildAction();
        enableAutoBuildAction = new EnableAutoBuildAction();
        disableAutoBuildAction = new DisableAutoBuildAction();
        refreshAction = new RefreshAction();
        openAppMonitorAction = new OpenAppMonitorAction();
        enableInjectMetricsAction = new EnableDisableInjectMetricsAction(true);
        disableInjectMetricsAction = new EnableDisableInjectMetricsAction(false);
        openPerformanceDashboardAction = new OpenPerformanceDashboardAction();
        openTektonDashboardAction = new OpenTektonDashboardAction();
        enableProjectAction = new EnableProjectAction();
        disableProjectAction = new DisableProjectAction();
        removeProjectAction = new RemoveProjectAction();
        addExistingProjectAction = new AddExistingProjectAction();
        showAllLogFilesAction = new ShowAllLogsAction();
        closeAllLogFilesAction = new CloseAllLogsAction();
        restartRunModeAction = new RestartRunModeAction();
        restartDebugModeAction = new RestartDebugModeAction();
        attachDebuggerAction = new AttachDebuggerAction();
        openShellAction = new OpenContainerShellAction();

        newProjectAction = new NewCodewindProjectAction();

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    handleDoubleClick(e);
            }
        });
        tree.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(Component component, int x, int y) {
                handlePopup(component, x, y);
            }
        });

        this.setLayout(new BorderLayout());
        this.add(new JBScrollPane(tree), BorderLayout.CENTER);
    }

    private static CodewindTreeModel getTreeModel() {
        return CodewindTreeModel.getInstance();
    }

    // The CodewindToolWindow will hold the handle to UpdateHandler containing all the update listeners
    // It should be at the 'plugin' level.
    private static UpdateHandler updateHandler;

    public void init() {
        updateHandler = UpdateHandler.getInstance();
        CoreUtil.initLogUpdatesNotificationGroup();
        CoreUtil.runAsync(() -> {
            tree.setModel(getTreeModel());
            CoreUtil.setUpdateHandler(getTreeModel());
            // Potentially, the Codewind tree model could be moved to the below handler
            CoreUtil.setToolWindowUpdateHandler(updateHandler);
            getTreeModel().updateAll();
            if (this.initialSelectedProject != null) {
                expandToProject(initialSelectedProject);
                initialSelectedProject = null;
            }
        });
    }

    public static UpdateHandler getToolWindowUpdateHandler() {
        return updateHandler;
    }

    // A unique Key used in conjunction with Project.getUserData and Project.putUserData to hold the SocketConsole references connected
    // to each console log.  This unique key is used to look up the SocketConsole related to each ToolWindow Content object.
    // Usage:
    // PUT:  Given a Content, create a new Key<SocketConsole> and add to this list.  putUserData(Key, SocketConsole) to be retrieved later
    //       at any time the Project is available.
    // GET:  Get the Key from the list, based on the Content, and then call getUserData to retrieve the SocketConsole
    private static final Map<Content, Key<SocketConsole>> KEY_SOCKETCONSOLE_MAP = new HashMap<>();

    // This does not necessarily have to be static since then, each IDE Window will have its own Map.
    public static Map<Content, Key<SocketConsole>> getLogsKeyMap() {
        // if (debug) // TODO for test purposes
        //    get all content logs from all Window instances
        //       ensure the size accurately reflects this number
        return KEY_SOCKETCONSOLE_MAP;
    }

    public void expandLocalTree() {
        // Expand the tree for the local connection
        Object root = getTreeModel().getRoot();
        Object child = getTreeModel().getChild(root, 0);
        if (child == null) {
            tree.expandPath(new TreePath(root));
        } else {
            tree.expandPath(new TreePath(new Object[]{root, child}));
        }
    }

    public void setInitialSelectedProject(Project project) {
        this.initialSelectedProject = project;
    }

    public void expandToProject(Project project) {
        String projectName = project.getName();
        TreeModel model = tree.getModel();
        Object root = model.getRoot();
        if (model.getChildCount(root) > 0) {
            Object connection = model.getChild(root, 0);
            int projectCount = model.getChildCount(connection);
            for (int i = 0; i < projectCount; i++ ) {
                Object child = model.getChild(connection, i);
                if (child instanceof CodewindApplication) {
                    CodewindApplication app = (CodewindApplication)child;
                    String name = app.getName();
                    if (projectName.equals(name)) {
                        tree.setSelectionPath(new TreePath(new Object[] {root, connection, app}));
                        return;
                    }
                }
            }
        }
    }

    private void handleDoubleClick(MouseEvent e) {
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null)
            return;
        Object node = treePath.getLastPathComponent();
        if (node instanceof LocalConnection) {
            InstallStatus status = CodewindManager.getManager().getInstallStatus();
            if (status.isInstalled()) {
                // Latest Codewind is installed
                if (!status.isStarted()) {
                    // need to start the latest Codewind
                    AnActionEvent actionEvent = AnActionEvent.createFromInputEvent(e, ActionPlaces.POPUP,
                            null, DataContext.EMPTY_CONTEXT, true, false);
                    startCodewindAction.actionPerformed(actionEvent);
                }
            } else if (status.hasInstalledVersions()) {
                // An older version of Codewind is installed
                AnActionEvent actionEvent = AnActionEvent.createFromInputEvent(e, ActionPlaces.POPUP,
                        null, DataContext.EMPTY_CONTEXT, true, false);
                updateCodewindAction.actionPerformed(actionEvent);
            } else {
                // No version of Codewind is installed
                AnActionEvent actionEvent = AnActionEvent.createFromInputEvent(e, ActionPlaces.POPUP,
                        null, DataContext.EMPTY_CONTEXT, true, false);
                installCodewindAction.actionPerformed(actionEvent);
            }
        } else if (node instanceof CodewindApplication) {
            CodewindApplication app = (CodewindApplication)node;
            final URL rootURL = app.getRootUrl();
            if (rootURL != null) {
                try {
                    com.intellij.ide.browsers.BrowserLauncher.getInstance().browse(rootURL.toURI());
                } catch (URISyntaxException use) {
                    Logger.log("Bad Application URL: " + rootURL);
                    System.out.println("*** Bad Application URL: " + rootURL);
                }
            }
        }
    }

    private void handlePopup(Component component, int x, int y) {
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            return;
        }
        Object node = treePath.getLastPathComponent();
        if (node instanceof LocalConnection) {
            LocalConnection connection = (LocalConnection) node;
            handleLocalConnectionPopup((LocalConnection) node, component, x, y);
        } else if (node instanceof RemoteConnection) {
            handleRemoteConnectionPopup((RemoteConnection) node, component, x, y);
        } else if (node instanceof CodewindApplication) {
            handleApplicationPopup((CodewindApplication) node, component, x, y);
        }
    }

    private void handleLocalConnectionPopup(LocalConnection connection, Component component, int x, int y) {
        DefaultActionGroup actions = new DefaultActionGroup("CodewindGroup", true);

        actions.add(newProjectAction);
        actions.add(addExistingProjectAction);
        actions.addSeparator();
        InstallStatus status = CodewindManager.getManager().getInstallStatus();
        if (status.isInstalled()) {
            // a supported version of Codewind is installed
            actions.add(uninstallCodewindAction);
        } else if (status.hasInstalledVersions()) {
            // an older version of Codewind is installed
            actions.add(updateCodewindAction);
        } else if (!status.isError()) {
            // No version of Codewind is installed
            actions.add(installCodewindAction);
        }
        if (status.isStarted()) {
            actions.add(stopCodewindAction);
        } else if (status.isInstalled()) {
            actions.add(startCodewindAction);
        }
        if (actions.getChildrenCount() > 0)
            actions.addSeparator();
        actions.add(refreshAction);

        // TODO remove this
        // actions.add(debugAction);

        ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu("CodewindTree", actions);
        popupMenu.getComponent().show(component, x, y);
    }

    private void handleRemoteConnectionPopup(RemoteConnection connection, Component component, int x, int y) {
        // TODO implement this
    }

    private void handleApplicationPopup(CodewindApplication application, Component component, int x, int y) {
        DefaultActionGroup actions = new DefaultActionGroup("CodewindApplicationGroup", true);

        actions.add(openApplicationAction);
        actions.add(openAppOverviewAction);
        actions.add(openShellAction);
        actions.addSeparator();
        // Guaranteed separator here
        int numItems = actions.getChildrenCount();
        if (application.hasMetricsDashboard()) {
            actions.add(openAppMonitorAction);
        }
        if (application.hasPerfDashboard()) {
            actions.add(openPerformanceDashboardAction);
        }
        if (application.canInjectMetrics()) {
            if (!application.isMetricsInjected()) {
                actions.add(enableInjectMetricsAction);
            } else {
                actions.add(disableInjectMetricsAction);
            }
        }
        if (actions.getChildrenCount() > numItems) {
            actions.addSeparator();
        }
        // Guaranteed separator here
        CodewindConnection connection = application.getConnection();
        if (connection != null) {
            ConnectionEnv.TektonDashboard tekton = connection.getTektonDashboard();
            if (tekton.hasTektonDashboard()) {
                actions.add(openTektonDashboardAction);
                actions.addSeparator();
            }
        }

        // Todo: Change this to Open in New Window or Current Window
        //        actions.add(openIdeaProjectAction);
        // actions.addSeparator();
        DefaultActionGroup logGroup = new DefaultActionGroup(CodewindUIBundle.message("ShowLogFilesMenu"), true);
        actions.add(logGroup);
        logGroup.add(showAllLogFilesAction);
        logGroup.add(closeAllLogFilesAction);
        actions.addSeparator();
        if (application != null && application.connection.isLocal() && application.isAvailable() && application.getProjectCapabilities().canRestart()) {
            actions.add(restartRunModeAction);
        }
        if (application != null && application.connection.isLocal() && application.isAvailable() && application.supportsDebug()) {
            actions.add(restartDebugModeAction);
        }
        if (application != null && application.connection.isLocal() && application.isAvailable() && ((CodewindIntellijApplication)application).canInitiateDebugSession()) {
            actions.add(attachDebuggerAction);
        }
        actions.addSeparator();
        actions.add(startBuildAction);
        if (application.isAutoBuild()) {
            actions.add(disableAutoBuildAction);
        } else {
            actions.add(enableAutoBuildAction);
        }

        actions.addSeparator();
        if (application.isAvailable()) {
            actions.add(disableProjectAction);
        } else {
            actions.add(enableProjectAction);
        }
        actions.add(removeProjectAction);

        actions.addSeparator();
        actions.add(refreshAction);

        ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu("CodewindTree", actions);
        popupMenu.getComponent().show(component, x, y);
    }
}
