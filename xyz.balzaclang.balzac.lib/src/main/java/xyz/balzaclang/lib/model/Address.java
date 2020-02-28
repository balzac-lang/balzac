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
import org.bitcoinj.core.LegacyAddress;

public interface Address {

    public byte[] getBytes();

    public String getWif();

    public String getBytesAsString();

    public static Address fromBase58(String wif) {
        LegacyAddress addr = LegacyAddress.fromBase58(null, wif);
        return new AddressImpl(addr.getHash(), NetworkType.from(addr.getParameters()));
    }

    public static Address fromPubkey(byte[] pubkey, NetworkType params) {
        LegacyAddress addr = LegacyAddress.fromKey(params.toNetworkParameters(), ECKey.fromPublicOnly(pubkey));
        return new AddressImpl(addr.getHash(), params);
    }

    public static Address from(Address address) {
        return fromBase58(address.getWif());
    }

    public static Address from(PublicKey pubkey, NetworkType params) {
        return from(pubkey.toAddress(params));
    }

    public static Address from(PrivateKey key) {
        return from(key.toAddress());
    }

    public static Address fresh(NetworkType params) {
        LegacyAddress addr = LegacyAddress.fromKey(params.toNetworkParameters(), new ECKey());
        return new AddressImpl(addr.getHash(), params);
    }
}
