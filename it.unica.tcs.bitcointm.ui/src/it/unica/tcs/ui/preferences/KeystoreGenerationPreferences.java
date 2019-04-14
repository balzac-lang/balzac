/*
 * Copyright 2019 Nicola Atzei
 */
package it.unica.tcs.ui.preferences;

import java.util.Arrays;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import it.unica.tcs.bitcointm.ui.internal.BitcointmActivator;
import it.unica.tcs.utils.SecureStorageUtils;

public class KeystoreGenerationPreferences extends PreferencePage implements IWorkbenchPreferencePage {

//    private Text oldPasswordText;
//    private Text newPasswordText;
    private Text repeatPasswordText;

    private ISecurePreferences secureStorage;

    /**
     * Create the preference page.
     */
    public KeystoreGenerationPreferences() {
        setTitle("Keystore generation");
        setPreferenceStore(BitcointmActivator.getInstance().getPreferenceStore());
    }

    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        secureStorage = SecurePreferencesFactory.getDefault();
    }

    /**
     * Create contents of the preference page.
     *
     * @param parent
     *            the parent composite
     */
    @Override
    public Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout gl_grpTestnet = new GridLayout(2, false);
        gl_grpTestnet.marginTop = 5;
        gl_grpTestnet.marginRight = 5;
        gl_grpTestnet.marginLeft = 5;
        gl_grpTestnet.marginBottom = 5;
        container.setLayout(gl_grpTestnet);

//        Label oldPasswordLabel = new Label(container, SWT.NONE);
//        oldPasswordLabel.setText("Old Password");
//
//        Text oldPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
//        oldPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//        oldPasswordText.setEnabled(isPasswordSaved());
//        if (!isPasswordSaved()) {
//            oldPasswordText.setMessage("Password not set");
//        }
//
//        Label newPasswordLabel = new Label(container, SWT.NONE);
//        newPasswordLabel.setText("New Password");
//
//        Text newPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
//        newPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label repeatPasswordLabel = new Label(container, SWT.NONE);
        repeatPasswordLabel.setText("Password");

        Text repeatPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
        repeatPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

//        this.oldPasswordText = oldPasswordText;
//        this.newPasswordText = newPasswordText;
        this.repeatPasswordText = repeatPasswordText;

        initialize(); // initialize properties values

        return container;
    }

    private void initialize() {
        if (isPasswordSaved()) {
//            newPasswordText.setMessage("Not changed");
            repeatPasswordText.setMessage("Not changed");
        }
    }

    @Override
    public void performDefaults() {
//        oldPasswordText.setEnabled(isPasswordSaved());
//        if (!isPasswordSaved()) {
//            oldPasswordText.setMessage("Password not set");
//        }
//        newPasswordText.setText("");
//        newPasswordText.setMessage("Enter a new password");
        repeatPasswordText.setText("");
        repeatPasswordText.setMessage("Enter a new password");
        super.updateApplyButton();
    }

    @Override
    public boolean isValid() {
        // check the old password
//        if (oldPasswordText.isEnabled()) {
//            try {
//                String pass = secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__KEYSTORE).get(SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD, "");
//                if (!pass.equals(oldPasswordText.getText())) {
//                    this.setErrorMessage("Wrong old password");
//                    return false;
//                }
//            }
//            catch (StorageException e) {
//                e.printStackTrace();
//                this.setErrorMessage("An error occurred reading the secure storage");
//                return false;
//            }
//        }
//
//        // check new == repeat
//        if (!newPasswordText.getText().equals(repeatPasswordText.getText())) {
//            this.setErrorMessage("Passwords do not match");
//            return false;
//        }

        return true;
    }

    @Override
    public boolean performOk() {

        // store
        if (!repeatPasswordText.getText().isEmpty()) {
            try {
                secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__KEYSTORE).put(
                        SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD, repeatPasswordText.getText(),
                        true);
            } catch (StorageException e) {
                e.printStackTrace();
                this.setErrorMessage("An error occurred reading the secure storage");
                return false;
            }
        }

        return true;
    }

    private boolean isPasswordSaved() {
        ISecurePreferences node = secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__KEYSTORE);
        return Arrays.asList(node.keys()).contains(SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD);
    }
}
