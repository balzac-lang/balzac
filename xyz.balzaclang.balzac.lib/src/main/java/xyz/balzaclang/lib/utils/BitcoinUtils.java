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

package xyz.balzaclang.lib.utils;

import java.math.BigInteger;
import java.nio.charset.Charset;

import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import xyz.balzaclang.lib.model.Hash;
import xyz.balzaclang.lib.model.Signature;

public class BitcoinUtils {

    public static Long sizeOf(Long value) {
        return Long.valueOf(Utils.encodeMPI(BigInteger.valueOf(value), false).length);
    }

    public static Long sizeOf(Boolean value) {
        return value? 1L : 0L;
    }

    public static Long sizeOf(String value) {
        return Long.valueOf(value.length());
    }

    public static String encode(byte[] bytes) {
        return Utils.HEX.encode(bytes);
    }

    public static byte[] decode(String chars) {
        return Utils.HEX.decode(chars.toLowerCase());
    }

    // TODO: move this to a new interface
    public static Script toScript(Object obj) {

        if (obj instanceof Number)
            return new ScriptBuilder().number(((Number) obj).longValue()).build();

        else if (obj instanceof String)
            return new ScriptBuilder().data(((String) obj).getBytes(Charset.forName("UTF-8"))).build();

        else if (obj instanceof Hash)
            return new ScriptBuilder().data(((Hash) obj).getBytes()).build();

        else if (obj instanceof Boolean)
            return ((Boolean) obj)? new ScriptBuilder().opTrue().build(): new ScriptBuilder().opFalse().build();

        else if (obj instanceof TransactionSignature)
            return new ScriptBuilder().data(((TransactionSignature) obj).encodeToBitcoin()).build();

        else if (obj instanceof Signature) {
            Signature sig = (Signature) obj;
            ScriptBuilder sb = new ScriptBuilder();
            sb.data(sig.getSignature());
            if (sig.getPubkey().isPresent())
                sb.data(sig.getPubkey().get().getBytes());
            return sb.build();
        }

        throw new IllegalArgumentException();
    }

}
