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
import com.intellij.ui.HyperlinkAdapter;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WidgetUtils {

    public static JTextArea createJTextArea(String textValue) {
        JTextArea textArea = new JTextArea();
        JTextField textField = new JTextField();
        textArea.setBorder(
                BorderFactory.createCompoundBorder(
                        textField.getBorder(),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6) // between border and text
                )
        );
        textArea.setSelectedTextColor(textField.getSelectedTextColor());
        textArea.setFont(textField.getFont());
        textArea.setOpaque(false);  // To keep background colour the same
        textArea.setText(textValue);
        textArea.setRequestFocusEnabled(true);
        return textArea;
    }

    public static JLabel createHyperlinkUsingLabel(String urlValue) {
        JLabel hyperlink = new JLabel(urlValue);
        hyperlink.setForeground(Color.BLUE.darker());  // TODO: Possible contrast issue?
        hyperlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        hyperlink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    BrowserLauncher.getInstance().browse(new URI(hyperlink.getText()));
//                    Desktop.getDesktop().browse(new URI(hyperlink.getText()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        JTextField tf = new JTextField();
        hyperlink.setBorder(
                BorderFactory.createCompoundBorder(
                        tf.getBorder(),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6) // between border and text
                )
        );
        return hyperlink;
    }

    public static JEditorPane createHyperlink(String urlValue) {
        JEditorPane editorPane = new JEditorPane();
        JTextField tf = new JTextField();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.setBorder( BorderFactory.createCompoundBorder(
                tf.getBorder(),
                BorderFactory.createEmptyBorder(3,6,3,6) // between border and text
             )
        );
        editorPane.setSelectedTextColor(tf.getSelectedTextColor());
        editorPane.setFont(tf.getFont()); // Set the font of the editorPane to be the same as a text field
        editorPane.setText(urlValue);
        final InputMap inputMap = editorPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final KeyStroke goKey = KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK);
        inputMap.put(goKey, "activate-link-action");
        editorPane.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent event) {
                try {
                    BrowserLauncher.getInstance().browse(event.getURL().toURI());
//                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return editorPane;
    }
}