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
package org.eclipse.codewind.intellij.ui.templates;

import org.eclipse.codewind.intellij.core.connection.RepositoryInfo;

import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RepoEntry {
    public final String url;
    public final String username;
    public final String password;
    public final String accessToken;
    public final String name;
    public final String description;
    public boolean enabled;
    public RepositoryInfo info;

    public RepoEntry(String url, String username, String password, String accessToken, String name, String description) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.accessToken = accessToken;
        this.name = name;
        this.description = description;
        this.enabled = true;
    }

    public RepoEntry(RepositoryInfo info) {
        this.url = info.getURL();
        this.username = null;
        this.password = null;
        this.accessToken = null;
        this.name = info.getName();
        this.description = info.getDescription();
        this.enabled = info.getEnabled();
        this.info = info;
    }

    public boolean isProtected() {
        if (info != null) {
            return info.isProtected();
        }
        return false;
    }

    public String getStyles() {
        if (info != null) {
            List<String> styles = info.getStyles();
            if (styles == null || styles.isEmpty()) {
                return message("GenericNotAvailable");
            }
            StringBuilder builder = new StringBuilder();
            boolean start = true;
            for (String style : styles) {
                if (!start) {
                    builder.append(", ");
                } else {
                    start = false;
                }
                builder.append(style);
                return builder.toString();
            }
        }
        return message("GenericNotAvailable");
    }
}
