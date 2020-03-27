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
package org.eclipse.codewind.intellij.ui.debug;

import com.intellij.execution.remote.RemoteConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Component;

public class CodewindDebugSettingsEditor extends RemoteConfigurable {

    public CodewindDebugSettingsEditor(Project project) {
        super(project);
    }

    /**
     * Our editor will reuse the built-in editor.  However, their widgets are private so this is a workaround to
     * hide the one JTextArea that shows the command line arguments and JDK option of the JVM process, which is
     * not customizable this way.  TODO: create simple JUnit test to test this method.
     *
     * @return
     */
    @NotNull
    @Override
    protected JComponent createEditor() {
        JComponent mainComp = super.createEditor();
        Component[] components = mainComp.getComponents();
        outer: for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] childComps = ((JPanel) comp).getComponents();
                for (Component child : childComps) {
                    if (child instanceof JTextArea) {
                        comp.setVisible(false);
                        break outer;
                    }
                }
            }
        }
        return mainComp;
    }

    /**
     * Internal - for automated test purposes only whenever the platform is updated
     *
     * From a registered CodewindConfigurationType, get the configuration, then get the editor.
     *
     * @return
     */
    @TestOnly
    public JComponent testCreateEditor() {
        return createEditor();
    }
}
