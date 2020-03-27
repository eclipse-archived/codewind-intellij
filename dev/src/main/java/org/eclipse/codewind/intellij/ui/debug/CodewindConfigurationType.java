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

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message;

public class CodewindConfigurationType extends SimpleConfigurationType {

    public static String ID = "org.eclipse.codewind.intellij.core.configuration.configurationType";

    @NotNull
    public static CodewindConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(CodewindConfigurationType.class);
    }

    public CodewindConfigurationType() {
        super(ID, message("DebugRunConfigurationType"), message("DebugRunConfigurationTypeDescription"), NotNullLazyValue.createValue(() -> IconCache.getCachedIcon(IconCache.ICONS_CODEWIND_13PX_SVG)));
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        // RemoteConfiguration
        return new CodewindDebugConfiguration(project, this);
    }
}
