/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.ui.preferences;

import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.xtext.ui.editor.preferences.AbstractPreferencePage;

import it.unica.tcs.bitcointm.ui.internal.BitcointmActivator;
import it.unica.tcs.ui.preferences.field.PasswordFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class BitcoinNodesPreferencePage extends AbstractPreferencePage {

    public BitcoinNodesPreferencePage() {
        setTitle("Nodes configuration");
        setDescription("Configure some trusted nodes to enable further static checks on your code");
        setPreferenceStore(BitcointmActivator.getInstance().getPreferenceStore());
        System.out.println("=== "+getPreferenceStore().getClass());
    }

    @Override
    public void createFieldEditors() {
        Group container = new Group(getFieldEditorParent(), SWT.NONE);
        Layout layout = new RowLayout();
        container.setLayout(layout);
        
        Group testnetBox = getNetworkBox(container, "Testnet node");
        Group mainnetBox = getNetworkBox(container, "Mainet node");
        
        // testnet node configuration
        StringFieldEditor testnetHost = new StringFieldEditor(PreferenceConstants.P_TESTNET_HOST, "&Host", testnetBox);
        IntegerFieldEditor testnetPort = new IntegerFieldEditor(PreferenceConstants.P_TESTNET_PORT, "&Port", testnetBox);
        StringFieldEditor testnetUsername = new StringFieldEditor(PreferenceConstants.P_TESTNET_USERNAME, "&Username", getFieldEditorParent());
//        PasswordFieldEditor testnetPassword = new PasswordFieldEditor(PreferenceConstants.P_TESTNET_PASSWORD, "&Password", testnetBox);
        IntegerFieldEditor testnetTimeout = new IntegerFieldEditor(PreferenceConstants.P_TESTNET_TIMEOUT, "&Timeout (msec)", getFieldEditorParent());
        
        testnetPort.setValidRange(1, 65535);

        addField(testnetHost);
        addField(testnetPort);
        addField(testnetUsername);
//        addField(testnetPassword);
        addField(testnetTimeout);
        
        // mainnet node configuration
        StringFieldEditor mainnetHost = new StringFieldEditor(PreferenceConstants.P_MAINNET_HOST, "&Host", getFieldEditorParent());
        IntegerFieldEditor mainnetPort = new IntegerFieldEditor(PreferenceConstants.P_MAINNET_PORT, "&Port", getFieldEditorParent());
        StringFieldEditor mainnetUsername = new StringFieldEditor(PreferenceConstants.P_MAINNET_USERNAME, "&Username", getFieldEditorParent());
//        PasswordFieldEditor mainnetPassword = new PasswordFieldEditor(PreferenceConstants.P_MAINNET_PASSWORD, "&Password", mainnetBox);
        IntegerFieldEditor mainnetTimeout = new IntegerFieldEditor(PreferenceConstants.P_MAINNET_TIMEOUT, "&Timeout (msec)", getFieldEditorParent());
        
        mainnetPort.setValidRange(1, 65535);

        addField(mainnetHost);
        addField(mainnetPort);
        addField(mainnetUsername);
//        addField(mainnetPassword);
        addField(mainnetTimeout);
    }

    private Group getNetworkBox(Composite parent, String text) {
        Group networkBox = new Group(parent, SWT.NONE);
        Layout layout = new GridLayout();
        networkBox.setLayout(layout);
        networkBox.setText(text);
        return networkBox;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }

}