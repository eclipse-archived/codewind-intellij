/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.ui.module;

import com.intellij.openapi.module.ModuleType;
import org.eclipse.codewind.intellij.ui.IconCache;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class CodewindModuleType extends ModuleType<CodewindModuleBuilder> {

    public static final String CODEWIND_MODULE= "CODEWIND_MODULE";

    private static final CodewindModuleType CODEWIND_MODULE_TYPE = new CodewindModuleType();

    private CodewindModuleType() {
        super(CODEWIND_MODULE);
    }

    public static CodewindModuleType getInstance() {
        return CODEWIND_MODULE_TYPE;
    }

    @NotNull
    @Override
    public CodewindModuleBuilder createModuleBuilder() {
        return new CodewindModuleBuilder();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @NotNull
    @Override
    public String getName() {
        return message("CodewindModuleName");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDescription() {
        return message("CodewindModuleDescription");
    }

    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return IconCache.getCachedIcon(IconCache.ICONS_THEMELESS_CODEWIND_GREY_SVG);
    }
}
