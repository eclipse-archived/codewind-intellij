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

package org.eclipse.codewind.intellij.ui.tree;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

import static org.eclipse.codewind.intellij.ui.IconCache.*;

import org.eclipse.codewind.intellij.core.*;
import org.eclipse.codewind.intellij.core.cli.InstallStatus;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
import org.eclipse.codewind.intellij.core.constants.AppStatus;
import org.eclipse.codewind.intellij.core.constants.BuildStatus;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.core.constants.ProjectType;
import org.jetbrains.annotations.NotNull;

public class CodewindTreeNodeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        setIcons(value);
        String text = getText(value);
        super.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);
        return this;
    }

    private void setIcons(Object value) {
        if (value instanceof ConnectionManager || value instanceof String) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_CODEWIND_SVG);
            setIcons(icon);
            return;
        }

        if (value instanceof CodewindConnection) {
            CodewindConnection connection = (CodewindConnection)value;
            Icon icon = getCachedIcon(ICONS_THEMED_LOCAL_DISCONNECTED_SVG);
            if (connection.isConnected()) {
                icon = getCachedIcon(ICONS_THEMED_LOCAL_CONNECTED_SVG);
            }
            setIcons(icon);
            return;
        }

        if (!(value instanceof CodewindApplication)) {
            return;
        }

        CodewindApplication app = (CodewindApplication) value;
        ProjectType type = app.projectType;
        if (type == ProjectType.TYPE_LIBERTY) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_MICROPROFILE_SVG);
            setIcons(icon);
            return;
        }

        if (type == ProjectType.TYPE_NODEJS) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_NODEJS_SVG);
            setIcons(icon);
            return;
        }

        if (type == ProjectType.TYPE_SPRING) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_SPRING_SVG);
            setIcons(icon);
            return;
        }

        if (type == ProjectType.TYPE_SWIFT) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_SWIFT_SVG);
            setIcons(icon);
            return;
        }

        ProjectLanguage lang = app.projectLanguage;
        if (lang.isGo()) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_GO_SVG);
            setIcons(icon);
        } else if (lang.isJava()) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_JAVA_SVG);
            setIcons(icon);
        } else if (lang.isJavaScript()) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_NODEJS_SVG1);
            setIcons(icon);
        } else if (lang.isPython()) {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_PYTHON_SVG);
            setIcons(icon);
        } else {
            Icon icon = getCachedIcon(ICONS_THEMELESS_PROJECT_TYPES_GENERIC_SVG);
            setIcons(icon);
        }
    }

    private void setIcons(Icon icon) {
        setOpenIcon(icon);
        setClosedIcon(icon);
        setLeafIcon(icon);
    }

    @NotNull
    private String getText(Object element) {
        if (element instanceof ConnectionManager) {
            return getText((ConnectionManager) element);
        }

        if (element instanceof CodewindConnection) {
            return getText((CodewindConnection) element);
        }

        if (element instanceof CodewindApplication) {
            CodewindApplication application = (CodewindApplication) element;
            return getText(application);
        }

        return element.toString();
    }

    @NotNull
    private String getText(CodewindApplication application) {
        StringBuilder builder = new StringBuilder(application.name);

        if (application.isEnabled()) {
            AppStatus appStatus = application.getAppStatus();
            BuildStatus buildStatus = application.getBuildStatus();

            if (appStatus == AppStatus.UNKNOWN && buildStatus == BuildStatus.UNKOWN) {
                builder.append(" [").append(AppStatus.UNKNOWN.displayString).append("]");
            }

            if (appStatus != AppStatus.UNKNOWN) {
                String displayString = appStatus.getDisplayString(application.getStartMode());
                builder.append(" [").append(displayString).append("]");
            }

            if (buildStatus != BuildStatus.UNKOWN) {
                String buildDetails = application.getBuildDetails();
                if (buildDetails != null && !buildDetails.isEmpty()) {
                    builder.append(" [")
                            .append(buildStatus.getDisplayString())
                            .append(": ")
                            .append(buildDetails).append("]");
                } else {
                    builder.append(" [").append(buildStatus.getDisplayString()).append("]");
                }
            }
        } else {
            builder.append(" [").append(message("CodewindProjectDisabled")).append("]");
        }
        return builder.toString();
    }

    @NotNull
    private String getText(CodewindConnection connection) {
        if (connection instanceof LocalConnection) {
            return getText((LocalConnection) connection);
        }
        return getText((RemoteConnection)connection);
    }

    @NotNull
    private String getText(LocalConnection connection) {
        String text = connection.getName();
        CodewindManager manager = CodewindManager.getManager();
        CodewindManager.InstallerStatus installerStatus = manager.getInstallerStatus();
        if (installerStatus != null) {
            switch (installerStatus) {
                case INSTALLING:
                    return text + " [" + message("CodewindInstallingQualifier") + "]";
                case UNINSTALLING:
                    return text + " [" + message("CodewindUninstallingQualifier") + "]";
                case STARTING:
                    return text + " [" + message("CodewindStartingQualifier") + "]";
                case STOPPING:
                    return text + " [" + message("CodewindStoppingQualifier") + "]";
            }
        } else {
            InstallStatus status = manager.getInstallStatus();
            if (status.isStarted()) {
                return text + " [" + message("CodewindRunningQualifier") + "]";
            } else if (status.isInstalled()) {
                if (status.hasStartedVersions()) {
                    // An older version is running
                    return text + " [" + message("CodewindWrongVersionQualifier", status.getStartedVersions()) + "] (" +
                            message("CodewindWrongVersionMsg", InstallUtil.getVersion());
                }
                return text + " [" + message("CodewindNotStartedQualifier") + "] (" + message("CodewindNotStartedMsg") + ")";
            } else if (status.hasInstalledVersions()) {
                // An older version is installed
                return text + " [" + message("CodewindWrongVersionQualifier", status.getInstalledVersions()) + "] (" +
                        message("CodewindWrongVersionMsg", InstallUtil.getVersion());
            } else if (status.isError()) {
                String msg = manager.getInstallerErrorMsg();
                if (msg != null) {
                    Logger.log(msg); // Log this as INFO so it will be in the IntelliJ log by default (trace does not have to be enabled)
                }
                return text + " [" + message("CodewindErrorQualifier") + "] (" + message("CodewindErrorMsg") + ")";
            } else if (status.isUnknown()) {
                return text + " [ " + message("RefreshingCodewindStatus") + " ]";
            } else {
                // Only remaining possibility is that codewind isn't installed.
                return text + " [" + message("CodewindNotInstalledQualifier") + "] (" + message("CodewindNotInstalledMsg") + ")";

            }
        }
        return text;
    }

    @NotNull
    private String getText(RemoteConnection connection) {
        // TODO implement this
        return "";
    }

    @NotNull
    private String getText(ConnectionManager manager) {
        return message("CodewindLabel");
    }
}
