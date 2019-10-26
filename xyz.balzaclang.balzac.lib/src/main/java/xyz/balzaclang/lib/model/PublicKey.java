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

import org.bitcoinj.core.ECKey;

import xyz.balzaclang.lib.utils.BitcoinUtils;

public interface PublicKey {

    public byte[] getBytes();

    public String getBytesAsString();

    public Address toAddress(NetworkType params);

    public Address toTestnetAddress();

    public Address toMainnetAddress();

    public static PublicKey fromBytes(byte[] pubkey) {
        return new PublicKeyImpl(pubkey);
    }

    public static PublicKey fromString(String str) {
        return fromBytes(BitcoinUtils.decode(str));
    }

    public static PublicKey fresh() {
        return fromBytes(new ECKey().getPubKey());
    }

    public static PublicKey from(PublicKey key) {
        return fromBytes(key.getBytes());
    }

    public static PublicKey from(PrivateKey key) {
        return from(key.toPublicKey());
    }
}
