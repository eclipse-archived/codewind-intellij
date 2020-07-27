/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core;

import java.net.URI;
import java.nio.file.Path;

import org.eclipse.codewind.intellij.core.cli.AuthToken;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.core.constants.ProjectType;

/**
 * Factory for creating the correct Codewind objects.  This is used to keep the Eclipse
 * code and the Codewind code separate.
 * 
 * Currently only CodewindApplication has an Eclipse version.  Rather than let Eclipse
 * code leak into CodewindConnection an Eclipse version of it should be created if necessary.
 */
public class CodewindObjectFactory  {


	public static CodewindConnection createRemoteConnection(String name, URI uri, String conid, String username, AuthToken authToken) {
		return new RemoteConnection(name, uri, conid, username, authToken);
	}

	public static CodewindApplication createCodewindApplication(CodewindConnection connection,
			String id, String name, ProjectType projectType, ProjectLanguage language, Path localPath) throws Exception {
		return new CodewindIntellijApplication(connection, id, name, projectType, language, localPath);
	}

}
