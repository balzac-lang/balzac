/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.ui.preferences.field;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class PasswordFieldEditor extends StringFieldEditor {

    private ISecurePreferences secureStorage;
    
    public PasswordFieldEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
        getTextControl(parent).setEchoChar('*');
        this.secureStorage = SecurePreferencesFactory.getDefault();
    }

    public PasswordFieldEditor(String name, String labelText, int width, Composite parent) {
        super(name, labelText, width, parent);
        getTextControl(parent).setEchoChar('*');
        this.secureStorage = SecurePreferencesFactory.getDefault();
    }

    public PasswordFieldEditor(String name, String labelText, int width, int strategy, Composite parent) {
        super(name, labelText, width, strategy, parent);
        getTextControl(parent).setEchoChar('*');
        this.secureStorage = SecurePreferencesFactory.getDefault();
    }
    
    public ISecurePreferences getSecureStorage() {
        return secureStorage;
    }

    public void setSecureStorage(ISecurePreferences secureStorage) {
        this.secureStorage = secureStorage;
    }

    @Override
    protected void doLoad() {
        System.out.println("Loading from secure storage");
        String password = "";
        try {
            password = secureStorage.get(getPreferenceName(), "");
        } catch (StorageException e) {
            e.printStackTrace();
        }
        setStringValue(password);
    }
    
    @Override
    protected void doLoadDefault() {
        System.out.println("Loading default password");
        setStringValue("");
        valueChanged();
    }
    
    @Override
    protected void doStore() {
        System.out.println("Storing password to secure storage");
        boolean encrypt = true;
        try {
            secureStorage.put(getPreferenceName(), getStringValue(), encrypt);
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }
}
