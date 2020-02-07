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
package org.eclipse.codewind.intellij.ui.wizard;

import org.eclipse.codewind.intellij.core.connection.ProjectTypeInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectInfo;
import org.jetbrains.annotations.SystemIndependent;

import java.util.Map;

/*
 * Model for Codewind Bind Wizard only
 */
public class BindProjectModel {

    public BindProjectModel() {
        // Empty
    }

    private String projectPath;
    private Map<String, ProjectTypeInfo> types;
    private ProjectInfo projectInfo;
    private ProjectTypeInfo projectTypeInfo;
    private ProjectTypeInfo.ProjectSubtypeInfo subtypeInfo;

    public void setProjectPath(String path) {
        this.projectPath = path;
    }

    @SystemIndependent
    public String getProjectPath() {
        return this.projectPath;
    }

    public void setTypes(Map<String, ProjectTypeInfo> types) {
        this.types = types;
    }

    public Map<String, ProjectTypeInfo> getTypes() {
        return types;
    }

    public void setProjectInfo(ProjectInfo info) {
        this.projectInfo = info;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectTypeInfo(ProjectTypeInfo info) {
        this.projectTypeInfo = info;
    }

    public ProjectTypeInfo getProjectTypeInfo() {
        return this.projectTypeInfo;
    }

    public void setSubtypeInfo(ProjectTypeInfo.ProjectSubtypeInfo subtypeInfo) {
        this.subtypeInfo = subtypeInfo;
    }

    public ProjectTypeInfo.ProjectSubtypeInfo getSubtypeInfo() {
        return subtypeInfo;
    }
}
