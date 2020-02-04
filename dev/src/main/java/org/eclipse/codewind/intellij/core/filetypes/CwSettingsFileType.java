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
package org.eclipse.codewind.intellij.core.filetypes;

import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle;
import org.jetbrains.annotations.NotNull;

public class CwSettingsFileType extends JsonFileType {
    public static final CwSettingsFileType INSTANCE = new CwSettingsFileType();
    public static final String DEFAULT_EXTENSION = "cw-settings";
    public static final String CwSettingsFileTypeName = "CwSettingsFileType"; // Non-NL. See plugin.xml

    private CwSettingsFileType() {
        super(JsonLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @NotNull
    @Override
    public String getName() {
        return CwSettingsFileTypeName;
    }

    @NotNull
    @Override
    public String getDescription() {
        return CodewindCoreBundle.message("CwSettingsFileTypeDescription"); // Shown in preferences
    }
}
