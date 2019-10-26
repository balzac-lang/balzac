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

import java.util.Arrays;

import org.bitcoinj.core.ECKey;

import xyz.balzaclang.lib.utils.BitcoinUtils;

class PrivateKeyImpl implements PrivateKey {

    private final NetworkType params;
    private final byte[] privkey;
    private final PublicKey pubkey;
    private final Address address;

    PrivateKeyImpl(byte[] privkey, NetworkType params) {
        this.params = params;
        this.privkey = Arrays.copyOf(privkey, privkey.length);
        this.pubkey = PublicKey.fromString(BitcoinUtils.encode(ECKey.fromPrivate(privkey).getPubKey()));
        this.address = Address.fromPubkey(pubkey.getBytes(), params);
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(privkey, privkey.length);
    }

    @Override
    public String getWif() {
        return ECKey.fromPrivate(privkey).getPrivateKeyAsWiF(params.toNetworkParameters());
    }

    @Override
    public String getBytesAsString() {
        return BitcoinUtils.encode(privkey);
    }

    @Override
    public PublicKey toPublicKey() {
        return pubkey;
    }

    @Override
    public Address toAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(privkey);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrivateKeyImpl other = (PrivateKeyImpl) obj;
        if (!Arrays.equals(privkey, other.privkey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getWif();
    }
}
