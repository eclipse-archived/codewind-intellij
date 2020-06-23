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
import org.eclipse.codewind.intellij.core.Logger;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    /**
     * Create a readonly JTextPane with a border
     * @param textValue
     * @return
     */
    public static JTextPane createTextPane(String textValue, boolean showBorder) {
        JTextPane textPane = new JTextPane();
        JTextField tf = new JTextField();
        textPane.setContentType("text/string");
        textPane.setEditable(false);
        textPane.setOpaque(false);
        if (showBorder) {
            textPane.setBorder(
                BorderFactory.createCompoundBorder(
                        tf.getBorder(),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6) // between border and text
                )
            );
        }
        textPane.setFont(tf.getFont()); // Set the font of the editorPane to be the same as a text field
        textPane.setSelectedTextColor(tf.getSelectedTextColor());
        textPane.setSelectionColor(tf.getSelectionColor());
        textPane.setText(textValue);
        textPane.setRequestFocusEnabled(true);
        textPane.setFocusable(true);
        return textPane;
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
                    Logger.logTrace(e);
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

    public static JTextPane createHyperlinkUsingTextPane(String urlValue, boolean showBorder) {
        JTextPane hyperlink = new JTextPane();
        JTextField tf = new JTextField();
        if (urlValue.length() == 0) {
            hyperlink.setContentType("text/string");
        } else {
            hyperlink.setContentType("text/html");
        }
        hyperlink.setEditable(false);
        hyperlink.setOpaque(false);
        if (showBorder) {
            hyperlink.setBorder(
                    BorderFactory.createCompoundBorder(
                            tf.getBorder(),
                            BorderFactory.createEmptyBorder(3, 6, 3, 6) // between border and text
                    )
            );
        }
        hyperlink.setFont(tf.getFont()); // Set the font of the editorPane to be the same as a text field
        hyperlink.setSelectedTextColor(tf.getSelectedTextColor());
        hyperlink.setSelectionColor(tf.getSelectionColor());
        hyperlink.setText(urlValue);
        hyperlink.setRequestFocusEnabled(true);
        hyperlink.setFocusable(true);
        final InputMap inputMap = hyperlink.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final KeyStroke goKey = KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK);
        inputMap.put(goKey, "activate-link-action");
        hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent event) {
                try {
                    BrowserLauncher.getInstance().browse(event.getURL().toURI());
                } catch (Exception e) {
                    Logger.logTrace(e);
                }
            }
        });
        return hyperlink;
    }

    public static JEditorPane createHyperlink(@Nonnull String urlValue) {
        JEditorPane editorPane = new JEditorPane();
        JTextField tf = new JTextField();
        if (urlValue.length() == 0) {
            editorPane.setContentType("text/string");
        } else {
            editorPane.setContentType("text/html");
        }
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
                } catch (Exception e) {
                    Logger.logTrace(e);
                }
            }
        });
        return editorPane;
    }

    public static String getTextValue(JTextField text) {
        return text.getText() == null || text.getText().trim().isEmpty() ? "" : text.getText().trim();
    }

    /**
     * Do not log password
     * @param passwordField
     * @return
     */
    public static String getPassword(JPasswordField passwordField) {
        char[] password = passwordField.getPassword();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < password.length; i++) {
            sb.append(password[i]);
        }
        return sb.toString();
    }
}