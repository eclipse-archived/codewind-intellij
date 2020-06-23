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

import com.intellij.ui.components.JBLabel;
import org.eclipse.codewind.intellij.ui.IconCache;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class WizardHeaderForm {
    private JPanel contentPane;
    private JPanel iconPanel;
    private JTextPane descriptionTextPane;
    private JLabel icon;
    private JFormattedTextField titleField;
    private JPanel textPanel;
    private JPanel headerPanel;

    private final String title, description;

    public WizardHeaderForm(String title, String description) {
        this.title = title;
        this.description = description;
    }

    private void createUIComponents() {
        titleField = new JFormattedTextField();
        titleField.setBorder(BorderFactory.createEmptyBorder());
        titleField.setText(title);
        descriptionTextPane = WidgetUtils.createTextPane(description, false);
        icon = new JBLabel();
        icon.setIcon(IconCache.getCachedIcon(IconCache.ICONS_CODEWIND_BANNER_PNG));
    }

    /**
     * Can be customized
     * @param customIcon
     */
    public void setIcon(Icon customIcon) {
        icon.setIcon(customIcon);
    }

    public JPanel getContentPane() {
        return this.contentPane;
    }
}
