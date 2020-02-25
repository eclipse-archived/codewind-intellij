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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import org.eclipse.codewind.intellij.core.CodewindManager;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.eclipse.codewind.intellij.core.cli.InstallStatus;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.ui.tasks.InstallCodewindTask;
import org.eclipse.codewind.intellij.ui.tasks.UpgradeCodewindTask;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class InstallCodewindStep extends ModuleWizardStep {

    private final JPanel panel;
    private final JLabel label;
    private final JButton button;
    private boolean isInstalled;

    public InstallCodewindStep() {
        this.panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        label = new JLabel(message("CodewindNotInstalled"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);

        String buttonLabel = null;
        LocalConnection localConnection = ConnectionManager.getManager().getLocalConnection();
        InstallStatus status = CodewindManager.getManager().getInstallStatus();
        if (status.hasInstalledVersions()) {
            buttonLabel = message("UpgradeCodewind");
        } else {
            buttonLabel = message("InstallCodewind");
        }
        button = new JButton(buttonLabel);
        panel.add(button);
        button.addActionListener(e -> {
            CodewindManager.getManager().refreshInstallStatus();
            if (status.isInstalled()) {
                CoreUtil.openDialog(CoreUtil.DialogType.INFO, message("CodewindLabel"), message("CodewindInstalledStarted"));
            } else if (status.hasInstalledVersions()) {
                ProgressManager.getInstance().run(new UpgradeCodewindTask(this::onInstall));
            } else {
                ProgressManager.getInstance().run(new InstallCodewindTask(this::onInstall));
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void updateDataModel() {
        // Implement this because it's abstract in superclass.
        // No model to update, so nothing to do.
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return isInstalled;
    }

    private void onInstall() {
        isInstalled = true;
        button.setEnabled(false);
        String txt = "<html>" + message("CodewindInstalledStarted") + "</html>";
        label.setText(txt);
    }
}
