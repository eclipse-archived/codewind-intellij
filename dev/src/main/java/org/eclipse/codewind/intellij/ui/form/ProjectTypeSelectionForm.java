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

import com.intellij.ui.components.JBList;
import org.eclipse.codewind.intellij.core.connection.ProjectTypeInfo;
import org.eclipse.codewind.intellij.core.connection.ProjectTypeInfo.ProjectSubtypeInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectInfo;
import org.eclipse.codewind.intellij.core.constants.ProjectLanguage;
import org.eclipse.codewind.intellij.core.constants.ProjectType;
import org.eclipse.codewind.intellij.ui.wizard.AbstractBindProjectWizardStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ProjectTypeSelectionForm {
    private AbstractBindProjectWizardStep step;
    private JLabel chooseProjectTypeLabel;
    private JList<ProjectTypeInfo> projectTypeList;
    private JLabel subtypeLabel;
    private JList<ProjectSubtypeInfo> subtypeList;
    private JLabel preferencesLabel;
    private JLabel manageTemplateSourcesLink;
    private JPanel contentPane;

    private DefaultListModel<ProjectTypeInfo> projectTypeListModel;
    private DefaultListModel<ProjectSubtypeInfo> subTypeListModel;
    private Map<String, ProjectTypeInfo> projectTypes;
    private ProjectInfo initialProjectInfo;
    private ProjectSubtypeInfo projectSubtypeInfo = null;

    public ProjectTypeSelectionForm(AbstractBindProjectWizardStep step) {
        this.step = step;
    }

    private void createUIComponents() {
        if (projectTypeListModel == null) {
            projectTypeListModel = new DefaultListModel<>();
        }
        if (subTypeListModel == null) {
            subTypeListModel = new DefaultListModel<>();
        }
        projectTypeList = new JBList<ProjectTypeInfo>(projectTypeListModel);
        projectTypeList.setCellRenderer(new ProjectTypeListCellRenderer<ProjectTypeInfo>());
        projectTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTypeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if (!listSelectionEvent.getValueIsAdjusting()) {
                    if (projectTypeList.getSelectedIndex() >= 0) {
                        Object selectedValue = projectTypeList.getSelectedValue();
                        int selectedIndex = projectTypeList.getSelectedIndex();
                        ProjectTypeInfo selectedType = projectTypeListModel.getElementAt(selectedIndex);
                        subtypeList.removeAll();
                        fillSubTypesList(false, selectedType);
                        step.fireStateChanging();
                    }
                }
            }
        });
        subtypeLabel = new JLabel(message("SelectProjectTypePageLanguageLabel"));
        subtypeList = new JBList<ProjectSubtypeInfo>(subTypeListModel);
        subtypeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                step.fireStateChanging();
            }
        });
        subtypeList.setCellRenderer(new SubTypeListCellRenderer<ProjectSubtypeInfo>());
        manageTemplateSourcesLink = WidgetUtils.createHyperlinkUsingLabel(message("SelectProjectTypeManageRepoLink"));
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public ProjectTypeInfo getSelectedProjectTypeInfo() {
        Object selectedValue = projectTypeList.getSelectedValue();
        return (ProjectTypeInfo) selectedValue;
    }

    @Nullable
    public ProjectSubtypeInfo getSelectedSubtype() {
        Object selectedValue = subtypeList.getSelectedValue();
        return (ProjectSubtypeInfo) selectedValue;
    }

    private void fillSubTypesList(boolean init, ProjectTypeInfo projectTypeInfo) {
        subtypeList.clearSelection();
        if (subTypeListModel == null) {
            subTypeListModel = new DefaultListModel<ProjectSubtypeInfo>();
        } else {
            subTypeListModel.removeAllElements();
        }
        boolean shouldShow = false;

        if (projectTypeInfo == null)
            subtypeList.clearSelection();
        else {
            List<ProjectSubtypeInfo> projectSubtypes = projectTypeInfo.getSubtypes();
            // Can only rely on the ID for any extensions to see if it is a java type
            for (ProjectSubtypeInfo subtypeInfo : projectSubtypes) {
                if (subtypeInfo.id.toLowerCase().contains(ProjectLanguage.LANGUAGE_JAVA.getId())) {
                    subTypeListModel.addElement(subtypeInfo);
                }
            }
            // only 1 possible choice, select it
            if (projectSubtypes.size() == 1) {
                subtypeList.setSelectedIndex(0);
            }
            // otherwise if more than 1 choice, allow subtype/language selection if:
            // 1. selected type is docker (can select language)
            // 2. selected type is different than the detected type
            //    e.g. project was detected as docker, then user switch selection to an
            //    extension project type, they should be allowed to choose the subtype
            else if (projectSubtypes.size() > 1) {
                boolean isDocker = projectTypeInfo.eq(ProjectType.TYPE_DOCKER);
                if (isDocker || initialProjectInfo == null || !projectTypeInfo.eq(initialProjectInfo.type)) {
                    // if possible, select language that was detected
                    if (init && isDocker && initialProjectInfo != null) {
                        projectSubtypeInfo = projectTypeInfo.new ProjectSubtypeInfo(initialProjectInfo.language.getId());
                        subtypeList.setSelectedValue(projectSubtypeInfo, true);
                    }
                    shouldShow = true;
                    String label = projectTypeInfo.getSubtypesLabel();
                    if ("".equals(label)) {
                        label = message("SelectProjectTypePageLanguageLabel");
                    } else {
                        label = message("SelectProjectTypePageSubtypeLabel", label);
                    }
                    subtypeLabel.setText(label);
                }
            }
        }
        if (!shouldShow) {
            subtypeLabel.setText("");
        }
        subtypeList.setVisible(shouldShow);
    }

    public void setProjectTypes(Map<String, ProjectTypeInfo> types) {
        this.projectTypes = types;
    }

    public void updateProjectTypesList(boolean init, Map<String, ProjectTypeInfo> types) {
        Set<String> keys = types.keySet();
        if (projectTypeListModel == null) {
            projectTypeListModel = new DefaultListModel<ProjectTypeInfo>();
        }
        if (init) {
            for (String s : keys) {
                ProjectTypeInfo info = types.get(s);
                if (s.equals(ProjectType.TYPE_DOCKER.getId()) || s.equals(ProjectType.TYPE_LIBERTY.getId()) ||
                        s.equals(ProjectType.TYPE_SPRING.getId()) || s.equals(ProjectType.TYPE_UNKNOWN.getId()) ||
                        "appsodyExtension".equals(s)) {
                    projectTypeListModel.addElement(info);
                }
            }
        }
        int selectIndex = -1;
        int length = projectTypeListModel.size();
        for (int i = 0; i < length; i++) {
            ProjectTypeInfo info = projectTypeListModel.get(i);
            if (info.getId().equals(initialProjectInfo.type.getId())) {
                selectIndex = i;
                break;
            }
        }
        if (selectIndex >= 0) {
            projectTypeList.setSelectedIndex(selectIndex);
        }
    }

    public void clearLists() {
        projectTypeList.removeAll();
        subtypeList.removeAll();
        if (projectTypeListModel != null) {
            projectTypeListModel.removeAllElements();
        }
        if (subTypeListModel != null) {
            subTypeListModel.removeAllElements();
        }
    }

    public void setInitialProjectInfo(ProjectInfo info) {
        boolean projectInfoChanged = true;
        if (this.initialProjectInfo != null && this.initialProjectInfo.type.getId().equals(info.type.getId()) &&
                this.initialProjectInfo.language.getId().equals(info.language.getId())) {
            projectInfoChanged = false;
        }
        this.initialProjectInfo = info;
        if (projectInfoChanged && projectTypes != null) {
            Set<String> keys = projectTypes.keySet();
            int i = 0;
            int selectIndex = -1;
            for (String s : keys) {
                ProjectTypeInfo anInfo = projectTypes.get(s);
                if (anInfo.getId().equals(initialProjectInfo.type.getId())) {
                    selectIndex = i;
                    break;
                }
                i++;
            }
            projectTypeList.setSelectedIndex(selectIndex);
        }
    }

    private static class SubTypeListCellRenderer<P> extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object o, int i, boolean b, boolean b1) {
            Component comp = super.getListCellRendererComponent(jList, o, i, b, b1);
            String label = ((ProjectSubtypeInfo) o).label;
            setText(ProjectLanguage.getDisplayName(label));
            return comp;
        }
    }

    private static class ProjectTypeListCellRenderer<P> extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object o, int i, boolean b, boolean b1) {
            Component comp = super.getListCellRendererComponent(jList, o, i, b, b1);
            ProjectTypeInfo projectTypeInfo = (ProjectTypeInfo) o;
            setText(ProjectType.getDisplayName(projectTypeInfo.getId()));
            return comp;
        }
    }
}
