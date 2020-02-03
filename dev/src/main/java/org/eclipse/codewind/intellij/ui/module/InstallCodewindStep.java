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
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;

import javax.swing.*;
import java.awt.*;

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

        button = new JButton(message("InstallCodewind"));
        panel.add(button);
        button.addActionListener(e -> {
            LocalConnection localConnection = ConnectionManager.getManager().getLocalConnection();
            localConnection.refreshInstallStatus();
            if (localConnection.getInstallStatus().isStarted()) {
                CoreUtil.openDialog(CoreUtil.DialogType.INFO, message("CodewindLabel"), message("CodewindInstalledStarted"));
            } else {
                // ProgressManager.getInstance().run(new InstallCodewindTask(this::onInstall));
                CoreUtil.openDialog(CoreUtil.DialogType.INFO, message("CodewindLabel"), "Not yet implemented");
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void updateDataModel() {

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
