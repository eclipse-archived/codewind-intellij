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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class CwSettingsFileTypeDetector implements FileTypeRegistry.FileTypeDetector {

    public CwSettingsFileTypeDetector() {
        CoreUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        FileTypeManager.getInstance().associate(CwSettingsFileType.INSTANCE, new ExactFileNameMatcher(CoreConstants.SETTINGS_FILE));
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public FileType detect(@NotNull VirtualFile file, @NotNull ByteSequence firstBytes, @Nullable CharSequence firstCharsIfText) {
        if (CoreConstants.SETTINGS_FILE.equals(file.getName())) {  // Fast.  Check only by filename
            return CwSettingsFileType.INSTANCE;
        }
        return null;
    }

    @Override
    /*
     * Cached - if detect method changes, update version
     */
    public int getVersion() {
        return 0;
    }
}
