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
package org.eclipse.codewind.intellij.ui.form;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.eclipse.codewind.intellij.core.constants.StartMode;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AppOverviewFrame {
    public JPanel overviewPanel;
    private JScrollPane overviewScrollPanel;
    private JPanel contentPanel;
    private JLabel appLabel;
    private JPanel columnPanel;
    private JPanel generalPanel;
    private JPanel rightPanel;
    private JSplitPane splitPanel;
    private JLabel typeLabel;
    private JTextField typeField;
    private JLabel languageLabel;
    private JTextField languageField;
    private JLabel locationOnDiskLabel;
    private JTextArea locationOnDiskTextArea;
    private JPanel projectSettingsPanel;
    private JPanel buildPanel;
    private JLabel applicationEndpointLabel;
    private JEditorPane applicationUrlLink;
    private JLabel externalApplicationPortLabel;
    private JTextField externalApplicationPortField;
    private JLabel externalDebugPortLabel;
    private JTextField externalDebugPortField;
    private JLabel projectIdLabel;
    private JTextArea projectIdTextArea;
    private JLabel containerIdLabel;
    private JTextArea containerIdTextArea;
    private JLabel statusLabel;
    private JTextField statusField;
    private JLabel applicationContextRootLabel;
    private JTextField applicationContextRootField;
    private JLabel internalApplicationPortLabel;
    private JTextField internalApplicationPortField;
    private JLabel internalDebugPortLabel;
    private JTextField internalDebugPortField;
    private JButton editProjectSettingsButton;
    private JButton moreInformationButton;
    private JPanel buttonPanel;
    private JLabel autoBuildLabel;
    private JTextField autoBuildField;
    private JLabel autoMetricsInjectionLabel;
    private JTextField autoMetricsInjectionField;
    private JLabel lastBuildLabel;
    private JFormattedTextField lastBuildField;
    private JLabel lastImageBuildLabel;
    private JTextField lastImageBuildField;
    private JSeparator separatorSpace;
    private JPanel footerPanel;
    private JPanel horizontalFiller;
    private JButton refreshButton;
    private JLabel preferencesLink;

    private CodewindApplication application;
    private final Project project;
    private boolean isHorizontal = true;

    public AppOverviewFrame(CodewindApplication application, Project project) {
        this.application = application;
        this.project = project;
    }

    private void createUIComponents() {
        final CodewindApplication app = this.application;
        final Project proj = this.project;
        overviewPanel = new JPanel();
        overviewPanel.setBackground(JBColor.background());
        // Top section showing app name
        appLabel = new JLabel(this.application.name);
        appLabel.setIcon(IconCache.getCachedIcon(IconCache.ICONS_THEMELESS_CODEWIND_SVG));

        columnPanel = new JPanel();
        splitPanel = new JSplitPane();
        // General Pane
        generalPanel = new JPanel();
        typeField = new JTextField(); // Type
        languageField = new JTextField(); // Language
        locationOnDiskTextArea = WidgetUtils.createJTextArea(""); // Location on Disk
        applicationUrlLink = WidgetUtils.createHyperlink(""); // Application Endpoint
        externalApplicationPortField = new JTextField(); // External/Exposed Application Port
        externalDebugPortField = new JTextField(); // External/Exposed Debug Port
        projectIdTextArea = WidgetUtils.createJTextArea(""); // Project ID
        containerIdTextArea = WidgetUtils.createJTextArea(""); // Container ID
        statusField = new JTextField(); // Status

        // Project Settings Pane
        rightPanel = new JPanel();
        applicationContextRootField = new JTextField(); // Application Context Root
        internalApplicationPortField = new JTextField(); // Internal Application Port
        internalDebugPortField = new JTextField(); // Internal Debug Port

        editProjectSettingsButton = new JButton();
        editProjectSettingsButton.setText(message("AppOverviewEditorEditProjectSettings"));
        editProjectSettingsButton.setToolTipText(message("AppOverviewEditorEditProjectSettingsTooltip"));
        editProjectSettingsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                File cwSettings = new File(app.fullLocalPath + File.separator + CoreConstants.SETTINGS_FILE);
                VirtualFile virtualFile = VfsUtil.findFileByIoFile(cwSettings, true);
                if (virtualFile != null && virtualFile.exists()) {
                    FileEditorManagerEx.getInstanceEx(proj).openFile(virtualFile, true);
                }
            }
        });
        moreInformationButton = new JButton();
        moreInformationButton.setText(message("AppOverviewEditorProjectSettingsInfo"));
        moreInformationButton.setToolTipText(message("AppOverviewEditorProjectSettingsInfoTooltip"));
        moreInformationButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    BrowserLauncher.getInstance().browse(new URI(UIConstants.GETTING_STARTED_INFO_URL));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        // Build Pane
        autoBuildField = new JTextField(); // Auto Build
        autoMetricsInjectionField = new JTextField(); // Auto Metrics Injection
        lastBuildField = new JFormattedTextField(); // Last Build
        lastImageBuildField = new JTextField(); // Last Image Build

        // Bottom footer
        preferencesLink = new JLabel(message("AppOverviewEditorPreferenceLink"));
        preferencesLink.setForeground(Color.BLUE.darker());
        preferencesLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        preferencesLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
            }
        });
        // Hide preferences link for now. Todo: Implement preferences
        preferencesLink.setVisible(false);

        refreshButton = new JButton();
        refreshButton.setText(message("AppOverviewEditorRefreshButton"));
        refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                CoreUtil.runAsync(new Runnable() { // Do NOT block the UI
                    @Override
                    public void run() {
                        app.connection.refreshApps(app.projectID);
                        CoreUtil.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                            update(app);
                        }
                        });
                    }
                });
            }
        });

        columnPanel.setLayout(new GridLayoutManager(2, 2));
        // Initialize with values
        updateProjectInfo();
        updateProjectStatus();
        updateAppInfoSection();
    }

    protected void updateProjectInfo() {
        typeField.setText(this.application.projectType.getDisplayName());
        languageField.setText(this.application.projectLanguage.getDisplayName());
        locationOnDiskTextArea.setText(this.application.fullLocalPath.toAbsolutePath().toString());
        StringBuffer sb = new StringBuffer();
        if (this.application.isAvailable() && this.application.getRootUrl() != null) {
            String rootUrl = this.application.getRootUrl().toString();
            // Use the same font as any Textfield, for consistency
            sb.append("<font face=\"" + languageField.getFont().getFontName() + "\"><a href='" + rootUrl + "'>" + rootUrl + "</a></font>");
        } else {
            sb.append("<font face=\"" + languageField.getFont().getFontName() + "\">" + message("AppOverviewEditorNotAvailable") + "</font>");
        }
        applicationUrlLink.setText(sb.toString());
        projectIdTextArea.setText(this.application.projectID);
    }

    public void updateProjectStatus() {
        autoBuildField.setText(this.application.isAutoBuild() ? message("AppOverviewEditorAutoBuildOn") : message("AppOverviewEditorAutoBuildOff"));
        autoMetricsInjectionField.setText(metricsInjectionState(this.application.canInjectMetrics(), this.application.isMetricsInjected()));
        long lastBuild = this.application.getLastBuild();
        String lastBuildStr = message("AppOverviewEditorProjectNeverBuilt");
        if (lastBuild > 0) {
            lastBuildStr = formatTimestamp(lastBuild);
        }
        lastBuildField.setText(lastBuildStr);
//      app.getBuildStatus().getDisplayString(), app.isAvailable());
        long lastImageBuild = this.application.getLastImageBuild();
        String lastImageBuildStr = message("AppOverviewEditorImageNeverBuilt");
        if (lastImageBuild > 0) {
            lastImageBuildStr = formatTimestamp(lastBuild);
        }
        lastImageBuildField.setText(lastImageBuildStr);

        if (this.application.isAvailable() && this.application.getStartMode() != null) {
            statusField.setText(this.application.getAppStatus().getDisplayString(this.application.getStartMode()));
        } else {
            statusField.setText(message("AppOverviewEditorNotAvailable"));
        }
    }

    public void updateAppInfoSection() {
        externalApplicationPortField.setText(this.application.isAvailable() && this.application.getHttpPort() > 0 ? Integer.toString(this.application.getHttpPort()) : message("AppOverviewEditorNotAvailable"));
        String hostDebugPort = null;
//        if (!this.application.connection.isLocal()) {
//            hostDebugPort = Messages.AppOverviewEditorNoDebugRemote;
//        } else if (this.application.supportsDebug()) {
        if (this.application.supportsDebug()) {
            if (this.application.getStartMode().equals(StartMode.DEBUG) || this.application.getStartMode().equals(StartMode.DEBUG_NO_INIT)) {
                hostDebugPort = this.application.isAvailable() && this.application.getDebugPort() > 0 ? Integer.toString(this.application.getDebugPort()) : message("AppOverviewEditorNotAvailable");
            } else {
                hostDebugPort = message("AppOverviewEditorNotDebugging");
            }
        } else {
            hostDebugPort = this.application.getCapabilitiesReady() ? message("AppOverviewEditorDebugNotSupported") : message("AppOverviewEditorNotAvailable");
        }
        externalDebugPortField.setText(hostDebugPort);

        String containerId = this.application.getContainerId();
        containerIdTextArea.setText(this.application.isAvailable() && containerId != null ? containerId : message("AppOverviewEditorNotAvailable"));

        applicationContextRootField.setText(this.application.getContextRoot() != null ? this.application.getContextRoot() : "/");
        String containerAppPort = this.application.getContainerAppPort();
        internalApplicationPortField.setText(containerAppPort != null ? containerAppPort : message("AppOverviewEditorNotAvailable"));

        String debugPort = null;
//        if (!app.connection.isLocal()) {
//            debugPort = message("AppOverviewEditorNoDebugRemote");
//        } else
        if (this.application.supportsDebug()) {
            debugPort = this.application.getContainerDebugPort();
        } else {
            debugPort = this.application.getCapabilitiesReady() ? message("AppOverviewEditorDebugNotSupported") : message("AppOverviewEditorNotAvailable");
        }
        internalDebugPortField.setText(debugPort);

        boolean hasSettingsFile = hasSettingsFile(this.application);
        editProjectSettingsButton.setEnabled(hasSettingsFile);
        moreInformationButton.setEnabled(hasSettingsFile);
    }

    public void update(CodewindApplication application) {
        this.application = application;
        if (application != null) {
            if (overviewPanel.isVisible()) {
                updateProjectInfo();
                updateProjectStatus();
                updateAppInfoSection();
            }
        } else {
            // message("AppOverviewEditorNoConnection") // For non-local connection, check connection as well
            String msg = message("AppOverviewEditorNoApplication");
            CoreUtil.openDialog(true, message("AppOverviewEditorNotAvailable"), msg);
        }
    }

    public String metricsInjectionState(boolean injectMetricsAvailable, boolean injectMetricsEnabled) {
        if (injectMetricsAvailable) {
            return (injectMetricsEnabled) ? message("AppOverviewEditorInjectMetricsOn") : message("AppOverviewEditorInjectMetricsOff");
        }
        return message("AppOverviewEditorInjectMetricsUnavailable");
    }

    private String formatTimestamp(long timestamp) {
        // Temporary - improve by showing how long ago the build happened
        Date date = new Date(timestamp);
        return date.toString();
    }

    private boolean hasSettingsFile(CodewindApplication app) {
        File cwSettings = new File(app.fullLocalPath + File.separator + CoreConstants.SETTINGS_FILE);
        VirtualFile virtualFile = VfsUtil.findFileByIoFile(cwSettings, true);
        if (virtualFile != null) {
            return virtualFile.exists();
        }
        return false;
    }

    public void relayout(boolean isHorizontal) {
        if (isHorizontal == this.isHorizontal) {
            return;
        }
        this.isHorizontal = isHorizontal;
        if (!isHorizontal) {
            //            columnPanel.remove(splitPanel);
            splitPanel.remove(generalPanel);
            splitPanel.remove(rightPanel);
            columnPanel.removeAll();
            columnPanel.setLayout(new GridLayoutManager(2, 2));

            columnPanel.add(generalPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, null, null, null));
            columnPanel.add(rightPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, null, null, null));

            splitPanel.setVisible(false);
            overviewScrollPanel.doLayout();
            overviewPanel.doLayout();
            columnPanel.doLayout();
        } else {
            splitPanel.setVisible(true);
            columnPanel.removeAll();
            columnPanel.setLayout(new GridLayoutManager(1, 2));
            columnPanel.add(splitPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK, null, null, null));
            splitPanel.setLeftComponent(generalPanel);
            splitPanel.setRightComponent(rightPanel);
            //            splitPanel.add(generalPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            //            splitPanel.add(rightPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
            //            columnPanel.doLayout();
        }
        columnPanel.invalidate();
        overviewPanel.invalidate();
//        overviewPanel.doLayout();
//        overviewPanel.updateUI();
    }
}
