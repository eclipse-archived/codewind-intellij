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
import org.eclipse.codewind.intellij.core.cli.InstallStatus;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.eclipse.codewind.intellij.ui.tasks.InstallCodewindTask;
import org.eclipse.codewind.intellij.ui.tasks.StartCodewindTask;
import org.eclipse.codewind.intellij.ui.tasks.UpgradeCodewindTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class CustomOptionStep extends ModuleWizardStep {

    private final JPanel panel;
    private final JLabel label;
    private final JButton button;

    private boolean isStarted;

    public CustomOptionStep() {
        this.panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        label = new JLabel();
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);

        button = new JButton();
        panel.add(button);

        configurePage();
    }

    private void removeActionListeners(JButton button) {
        for (ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
    }

    private void configurePage() {
        try {
            CodewindManager.getManager().refreshInstallStatus();
            InstallStatus status = CodewindManager.getManager().getInstallStatus();

            if (status.isStarted()) {
                label.setVisible(false);
                button.setVisible(false);
                isStarted = true;
            } else if (status.isError()) {
                String labelText = message("CodewindStatusError", CodewindManager.getManager().getInstallerErrorMsg());
                label.setText(labelText);
                button.setText(message("RefreshCodewindStatus"));
                removeActionListeners(button);
                button.addActionListener(e -> {
                    CodewindManager.getManager().refreshInstallStatus();
                    configurePage();
                });
            } else if (status.isInstalled()) {
                label.setText(message("CodewindNotStarted"));
                button.setText(message("StartCodewind"));
                removeActionListeners(button);
                button.addActionListener(e -> {
                    CodewindManager.getManager().refreshInstallStatus();
                    if (CodewindManager.getManager().getInstallStatus().isStarted()) {
                        CoreUtil.openDialog(CoreUtil.DialogType.INFO, message("CodewindLabel"), message("CodewindStarted"));
                    } else {
                        ProgressManager.getInstance().run(new StartCodewindTask(this::onStart));
                    }
                });
            } else if (status.hasInstalledVersions()) {
                label.setText(message("CodewindNotInstalled"));
                button.setText(message("UpgradeCodewind"));
                removeActionListeners(button);
                button.addActionListener(e -> {
                    CodewindManager.getManager().refreshInstallStatus();
                    if (CodewindManager.getManager().getInstallStatus().isStarted()) {
                        CoreUtil.openDialog(CoreUtil.DialogType.INFO, message("CodewindLabel"), message("CodewindStarted"));
                    } else {
                        ProgressManager.getInstance().run(new UpgradeCodewindTask(this::onStart));
                    }
                });
            } else {
                label.setText(message("CodewindNotInstalled"));
                button.setText(message("InstallCodewind"));
                removeActionListeners(button);
                button.addActionListener(e -> {
                    CodewindManager.getManager().refreshInstallStatus();
                    if (CodewindManager.getManager().getInstallStatus().isStarted()) {
                        CoreUtil.openDialog(CoreUtil.DialogType.INFO, message("CodewindLabel"), message("CodewindStarted"));
                    } else {
                        ProgressManager.getInstance().run(new InstallCodewindTask(this::onStart));
                    }
                });
            }
        } catch (Exception e) {
            Throwable cause = Logger.unwrap(e);
            Logger.logWarning(cause);
        }
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void updateDataModel() {
        // Need to override because it's abstract in superclass
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return isStarted;
    }

    private void onStart() {
        isStarted = true;
        button.setEnabled(false);
        String txt = "<html>" + message("CodewindStarted") + "</html>";
        label.setText(txt);
    }
}
