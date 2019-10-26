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

import org.bitcoinj.core.LegacyAddress;

import xyz.balzaclang.lib.utils.BitcoinUtils;

class AddressImpl implements Address {

    private final byte[] address;
    protected final NetworkType params;

    AddressImpl(byte[] address, NetworkType params) {
        this.address = address;
        this.params = params;
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(address, address.length);
    }

    @Override
    public String getWif() {
        return LegacyAddress.fromPubKeyHash(params.toNetworkParameters(), address).toBase58();
    }

    @Override
    public String getBytesAsString() {
        return BitcoinUtils.encode(address);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(address);
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
        AddressImpl other = (AddressImpl) obj;
        if (!Arrays.equals(address, other.address))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getWif();
    }
}
