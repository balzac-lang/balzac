/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.balzaclang.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;

import xyz.balzaclang.balzac.ui.internal.BalzacActivator;

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
        IPreferenceStore store = BalzacActivator.getInstance().getPreferenceStore();
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
