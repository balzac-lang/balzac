/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;

import it.unica.tcs.bitcointm.ui.internal.BitcointmActivator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore store = BitcointmActivator.getInstance().getPreferenceStore();
        store.setDefault(PreferenceConstants.P_TESTNET_HOST, "localhost");
        store.setDefault(PreferenceConstants.P_TESTNET_PORT, 18332);
        store.setDefault(PreferenceConstants.P_TESTNET_URL, "/");
        store.setDefault(PreferenceConstants.P_TESTNET_USERNAME, "bitcoin");
        store.setDefault(PreferenceConstants.P_TESTNET_TIMEOUT, 3000);
        store.setDefault(PreferenceConstants.P_TESTNET_HTTPS, false);
        store.setDefault(PreferenceConstants.P_MAINNET_HOST, "localhost");
        store.setDefault(PreferenceConstants.P_MAINNET_PORT, 8332);
        store.setDefault(PreferenceConstants.P_MAINNET_URL, "/");
        store.setDefault(PreferenceConstants.P_MAINNET_USERNAME, "bitcoin");
        store.setDefault(PreferenceConstants.P_MAINNET_TIMEOUT, 3000);
        store.setDefault(PreferenceConstants.P_MAINNET_HTTPS, false);

        try {
            TrustedNodesPreferences.setBitcoinClientFactoryNodes(store);
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

}
