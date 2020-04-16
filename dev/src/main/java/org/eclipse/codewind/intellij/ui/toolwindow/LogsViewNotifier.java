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
package org.eclipse.codewind.intellij.ui.toolwindow;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.console.ILogChangeNotifier;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.eclipse.codewind.intellij.ui.tree.CodewindToolWindowHelper;

import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import java.util.Enumeration;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class LogsViewNotifier implements ILogChangeNotifier {

    private static final String HTML_HREF = "href"; // Do not externalize

    private Project project;
    private String message = "";

    public LogsViewNotifier(Project project) {
        this.project = project;
    }

    @Override
    public synchronized void notifyChange(String displayName) {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        Balloon logFilesWindowBalloon = toolWindowManager.getToolWindowBalloon(CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID);
        String htmlContent = message("LogFilesNotification"); // "Changed Logs:\n";
        // Do not externalize these strings.  These are HTML links.
        if (logFilesWindowBalloon != null) {
            String originalMessage = message;
            if (message.contains(displayName)) {
                message = message.replace("<a href='" + displayName + "'>" + displayName + "</a>\n", "");
            }
            message = "<a href='" + displayName + "'>" + displayName + "</a>\n" + message;
            if (originalMessage.equals(message)) {
                return; // No need to notify again since the log file is already in the notification
            }
        } else {
            message = "<a href='" + displayName + "'>" + displayName + "</a>\n";
        }
        htmlContent = htmlContent.concat(message);
        Notification notification = CoreUtil.getLogUpdatesNotification().createNotification();
        notification.setContent(htmlContent);
        notification.setIcon(IconCache.getCachedIcon(IconCache.ICONS_THEMELESS_CODEWIND_SVG));
        notification.setListener((aNotification, hyperlinkEvent) -> {
            HyperlinkEvent.EventType eventType = hyperlinkEvent.getEventType();
            if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
                Element sourceElement = hyperlinkEvent.getSourceElement();
                AttributeSet attributes = sourceElement.getAttributes();
                Enumeration<?> attributeNames = attributes.getAttributeNames();
                ContentManager contentManager = toolWindowManager.getToolWindow(CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID).getContentManager();
                ToolWindow logWindow = toolWindowManager.getToolWindow(CodewindToolWindowHelper.SHOW_LOG_FILES_TOOLWINDOW_ID);
                while (attributeNames.hasMoreElements()) {
                    Object o = attributeNames.nextElement();
                    if (o instanceof HTML.Attribute) {
                        HTML.Attribute attr = (HTML.Attribute) o;
                        String attrString = attr.toString();
                        if (HTML_HREF.equals(attrString)) {
                            Object attribute = attributes.getAttribute(attr);
                            String logFile = attribute.toString();
                            Content content = contentManager.findContent(logFile);
                            if (logWindow != null && !logWindow.isVisible() || !logWindow.isActive()) {
                                logWindow.show(null);
                            }
                            if (content != null) {
                                contentManager.setSelectedContent(content);
                            }
                            break;
                        }
                    }
                }
            }
        });
        notification.notify(project);
    }
}
