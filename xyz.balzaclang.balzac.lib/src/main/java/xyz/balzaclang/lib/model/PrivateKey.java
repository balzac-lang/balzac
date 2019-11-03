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

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

public interface PrivateKey {

    public byte[] getBytes();

    public String getWif();

    public String getBytesAsString();

    public PublicKey toPublicKey();

    public Address toAddress();

    public NetworkType getNetworkType();

    public static PrivateKey fromBase58(String wif) {
        DumpedPrivateKey key = DumpedPrivateKey.fromBase58(null, wif);
        return from(key.getKey().getPrivKeyBytes(), NetworkType.from(key.getParameters()));
    }

    public static PrivateKey from(byte[] keyBytes, NetworkType params) {
        return new PrivateKeyImpl(keyBytes, params);
    }

    public static PrivateKey fresh(NetworkType params) {
        return from(new ECKey().getPrivKeyBytes(), params);
    }

    public static PrivateKey copy(PrivateKey key, NetworkType params) {
        return from(key.getBytes(), params);
    }

}
