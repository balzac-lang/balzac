/*
 * Copyright 2021 Nicola Atzei
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

    public boolean compressPublicKey();

    public String getWif();

    public String getBytesAsString();

    public PublicKey toPublicKey();

    public Address toAddress();

    public NetworkType getNetworkType();

    public PrivateKey withNetwork(NetworkType networkType);

    public static PrivateKey fromBase58(String wif) {
        DumpedPrivateKey key = DumpedPrivateKey.fromBase58(null, wif);
        byte[] keyBytes = key.getKey().getPrivKeyBytes();
        boolean compressPubkey = key.isPubKeyCompressed();
        return from(keyBytes, compressPubkey, NetworkType.from(key.getParameters()));
    }

    public static PrivateKey from(byte[] keyBytes, boolean compressPubkey, NetworkType params) {
        return new PrivateKeyImpl(keyBytes, compressPubkey, params);
    }

    public static PrivateKey fresh(NetworkType params) {
        ECKey key = new ECKey();
        return from(key.getPrivKeyBytes(), key.isCompressed(), params);
    }
}
