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
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.core.connection.LocalConnection;
import org.eclipse.codewind.intellij.ui.tasks.StartCodewindTask;

import javax.swing.*;
import java.awt.*;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class StartCodewindStep extends ModuleWizardStep {

    private final JPanel panel;
    private final JLabel label;
    private final JButton button;
    private boolean isStarted;

    public StartCodewindStep() {
        this.panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        label = new JLabel(message("CodewindNotStarted"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);

        button = new JButton(message("StartCodewind"));
        panel.add(button);
        button.addActionListener(e -> {
            LocalConnection localConnection = ConnectionManager.getManager().getLocalConnection();
            localConnection.refreshInstallStatus();
            if (localConnection.getInstallStatus().isStarted()) {
                CoreUtil.openDialog(CoreUtil.DialogType.INFO, message("CodewindLabel"), message("CodewindStarted"));
            } else {
                ProgressManager.getInstance().run(new StartCodewindTask(this::onStart));
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
        return isStarted;
    }

    private void onStart() {
        isStarted = true;
        button.setEnabled(false);
        String txt = "<html>" + message("CodewindStarted") + "</html>";
        label.setText(txt);
    }
}
