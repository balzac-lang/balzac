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
package xyz.balzaclang.lib.model;

import org.bitcoinj.core.NetworkParameters;

public enum NetworkType {

    MAINNET, TESTNET;

    public boolean isTestnet() {
        return this == TESTNET;
    }

    public boolean isMainnet() {
        return this == MAINNET;
    }

    public NetworkParameters toNetworkParameters() {
        return this == TESTNET ? NetworkParameters.fromID(NetworkParameters.ID_TESTNET)
            : NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
    }

    public static NetworkType from(NetworkParameters parameters) {
        return parameters.getId().equals(NetworkParameters.ID_TESTNET) ? TESTNET : MAINNET;
    }
}
