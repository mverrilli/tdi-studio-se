// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.preferencepage;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a
 * page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class ComponentProjectPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ComponentProjectPreferencePage() {
        super(GRID);
        setPreferenceStore(ComponentDesigenerPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void performApply() {
        super.performApply();
    }

    public void createFieldEditors() {
        Label l = new Label(getFieldEditorParent(), SWT.NONE);
        l.setText(Messages.getString("ComponentProjectPreferencePage.ChooseProject")); //$NON-NLS-1$
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        l.setLayoutData(gd);
        DirectoryFieldEditor filePathTemp = new DirectoryFieldEditor(PluginConstant.PROJECT_URL, Messages
                .getString("ComponentProjectPreferencePage.ComponentProject"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(filePathTemp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {

        super.propertyChange(event);

        MessageDialog.openWarning(getFieldEditorParent().getShell(),
                Messages.getString("ComponentProjectPreferencePage.Warning"), //$NON-NLS-1$
                Messages.getString("ComponentProjectPreferencePage.WarningMSG") //$NON-NLS-1$
                        + event.getOldValue());

    }

    public void init(IWorkbench workbench) {
    }

}
