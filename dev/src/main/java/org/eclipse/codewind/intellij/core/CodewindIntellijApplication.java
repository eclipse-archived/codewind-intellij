/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core;

import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.core.constants.ProjectType;

import java.net.MalformedURLException;
import java.nio.file.Path;

public class CodewindIntellijApplication extends CodewindApplication {

    CodewindIntellijApplication(CodewindConnection connection, String id, String name, ProjectType projectType,
                                ProjectLanguage language, Path localPath) throws MalformedURLException {
        super(connection, id, name, projectType, language, localPath);
    }

}
