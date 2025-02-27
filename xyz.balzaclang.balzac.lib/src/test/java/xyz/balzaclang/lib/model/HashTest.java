/*
 * Copyright 2022 Nicola Atzei
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.junit.Test;

import xyz.balzaclang.lib.model.Hash.HashAlgorithm;
import xyz.balzaclang.lib.utils.BitcoinUtils;

public class HashTest {

    @Test
    public void testSha1Digest() {
        String valueToHash = "Hello world!";
        String actual = Hash.sha1(valueToHash).getBytesAsString();
        String expected = "d3486ae9136e7856bc42212385ea797094475802";
        assertEquals(expected, actual);
    }

    @Test
    public void testSha256Digest() {
        String valueToHash = "Hello world!";
        String actual = Hash.sha256(valueToHash).getBytesAsString();
        String expected = "c0535e4be2b79ffd93291305436bf889314e4a3faec05ecffcbb7df31ad9e51a";
        assertEquals(expected, actual);
    }

    @Test
    public void testRipemd160Digest() {
        String valueToHash = "Hello world!";
        String actual = Hash.ripemd160(valueToHash).getBytesAsString();
        String expected = "7f772647d88750add82d8e1a7a3e5c0902a346a3";
        assertEquals(expected, actual);
    }

    @Test
    public void testHash256Digest() {
        String valueToHash = "Hello world!";
        String actual = Hash.hash256(valueToHash).getBytesAsString();
        String expected = "7982970534e089b839957b7e174725ce1878731ed6d700766e59cb16f1c25e27";
        assertEquals(expected, actual);
    }

    @Test
    public void testHash160Digest() {
        String valueToHash = "Hello world!";
        String actual = Hash.hash160(valueToHash).getBytesAsString();
        String expected = "621281c15fb62d5c6013ea29007491e8b174e1b9";
        assertEquals(expected, actual);
    }

    @Test
    public void testCompareTo() {
        Hash a = Hash.fromString("000000");
        Hash b = Hash.fromString("000123");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    public void testCompareToEqualsSameLength() {
        Hash a = Hash.fromString("00000123");
        Hash b = Hash.fromString("00000123");
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    public void testCompareToEqualsOfDifferentLength() {
        Hash a = Hash.fromString("000123");
        Hash b = Hash.fromString("00000123");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    public void test_hash_refl() {
        HashAlgorithm[] hashClasses = new HashAlgorithm[] { HashAlgorithm.HASH160, HashAlgorithm.HASH256,
            HashAlgorithm.RIPEMD160, HashAlgorithm.SHA256, HashAlgorithm.SHA1 };
        Object[] values = new Object[] { 1, 1L, "", true, new byte[42], new Hash(new byte[20]), new Hash(new byte[20]),
            new Hash(new byte[32]), new Hash(new byte[32]) };

        for (HashAlgorithm hashCls : hashClasses) {
            for (Object v : values) {
                assertArrayEquals(executeScript(v, hashCls), Hash.hash(v, hashCls).getBytes());
            }
        }
    }

    @Test
    public void test_hashes_empty() {
        assertArrayEquals(executeScript(new byte[] {}, HashAlgorithm.SHA1), Hash.sha1(new byte[] {}).getBytes());
        assertArrayEquals(executeScript(new byte[] {}, HashAlgorithm.RIPEMD160),
            Hash.ripemd160(new byte[] {}).getBytes());
        assertArrayEquals(executeScript(new byte[] {}, HashAlgorithm.SHA256), Hash.sha256(new byte[] {}).getBytes());
        assertArrayEquals(executeScript(new byte[] {}, HashAlgorithm.HASH160), Hash.hash160(new byte[] {}).getBytes());
        assertArrayEquals(executeScript(new byte[] {}, HashAlgorithm.HASH256), Hash.hash256(new byte[] {}).getBytes());
        assertArrayEquals(executeScript(new byte[] {}, HashAlgorithm.HASH256),
            Hash.sha256(Hash.sha256(new byte[] {})).getBytes());
        assertArrayEquals(executeScript(new byte[] {}, HashAlgorithm.HASH160),
            Hash.ripemd160(Hash.sha256(new byte[] {})).getBytes());
        assertArrayEquals(executeScript(executeScript(new byte[] {}, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
            Hash.hash256(new byte[] {}).getBytes());
        assertArrayEquals(executeScript(executeScript(new byte[] {}, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
            Hash.hash160(new byte[] {}).getBytes());
    }

    @Test
    public void test_hashes_boolean() {
        assertArrayEquals(executeScript(true, HashAlgorithm.SHA1), Hash.sha1(true).getBytes());
        assertArrayEquals(executeScript(true, HashAlgorithm.RIPEMD160), Hash.ripemd160(true).getBytes());
        assertArrayEquals(executeScript(true, HashAlgorithm.SHA256), Hash.sha256(true).getBytes());
        assertArrayEquals(executeScript(true, HashAlgorithm.HASH160), Hash.hash160(true).getBytes());
        assertArrayEquals(executeScript(true, HashAlgorithm.HASH256), Hash.hash256(true).getBytes());
        assertArrayEquals(executeScript(true, HashAlgorithm.HASH256), Hash.sha256(Hash.sha256(true)).getBytes());
        assertArrayEquals(executeScript(true, HashAlgorithm.HASH160), Hash.ripemd160(Hash.sha256(true)).getBytes());
        assertArrayEquals(executeScript(executeScript(true, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
            Hash.hash256(true).getBytes());
        assertArrayEquals(executeScript(executeScript(true, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
            Hash.hash160(true).getBytes());

        assertArrayEquals(executeScript(false, HashAlgorithm.SHA1), Hash.sha1(false).getBytes());
        assertArrayEquals(executeScript(false, HashAlgorithm.RIPEMD160), Hash.ripemd160(false).getBytes());
        assertArrayEquals(executeScript(false, HashAlgorithm.SHA256), Hash.sha256(false).getBytes());
        assertArrayEquals(executeScript(false, HashAlgorithm.HASH160), Hash.hash160(false).getBytes());
        assertArrayEquals(executeScript(false, HashAlgorithm.HASH256), Hash.hash256(false).getBytes());
        assertArrayEquals(executeScript(false, HashAlgorithm.HASH256), Hash.sha256(Hash.sha256(false)).getBytes());
        assertArrayEquals(executeScript(false, HashAlgorithm.HASH160), Hash.ripemd160(Hash.sha256(false)).getBytes());
        assertArrayEquals(executeScript(executeScript(false, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
            Hash.hash256(false).getBytes());
        assertArrayEquals(executeScript(executeScript(false, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
            Hash.hash160(false).getBytes());
    }

    @Test
    public void test_hashes_integers() {
        for (long i = -100; i < 100; i++) {
            assertArrayEquals(executeScript(i, HashAlgorithm.SHA1), Hash.sha1(i).getBytes());
            assertArrayEquals(executeScript(i, HashAlgorithm.RIPEMD160), Hash.ripemd160(i).getBytes());
            assertArrayEquals(executeScript(i, HashAlgorithm.SHA256), Hash.sha256(i).getBytes());
            assertArrayEquals(executeScript(i, HashAlgorithm.HASH160), Hash.hash160(i).getBytes());
            assertArrayEquals(executeScript(i, HashAlgorithm.HASH256), Hash.hash256(i).getBytes());
            assertArrayEquals(executeScript(i, HashAlgorithm.HASH256), Hash.sha256(Hash.sha256(i)).getBytes());
            assertArrayEquals(executeScript(i, HashAlgorithm.HASH160), Hash.ripemd160(Hash.sha256(i)).getBytes());
            assertArrayEquals(executeScript(executeScript(i, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
                Hash.hash256(i).getBytes());
            assertArrayEquals(executeScript(executeScript(i, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
                Hash.hash160(i).getBytes());
        }
    }

    private byte[] executeScript(Object input, HashAlgorithm alg) {
        if (input instanceof byte[])
            return executeScript((byte[]) input, alg);
        return executeScript(BitcoinUtils.toScript(input), alg);
    }

    private byte[] executeScript(byte[] script, HashAlgorithm alg) {
        return executeScript(new ScriptBuilder().data(script).build(), alg);
    }

    private byte[] executeScript(Script s, HashAlgorithm alg) {
        int operation;

        switch (alg) {
        case HASH160:
            operation = ScriptOpCodes.OP_HASH160;
            break;
        case HASH256:
            operation = ScriptOpCodes.OP_HASH256;
            break;
        case RIPEMD160:
            operation = ScriptOpCodes.OP_RIPEMD160;
            break;
        case SHA256:
            operation = ScriptOpCodes.OP_SHA256;
            break;
        case SHA1:
            operation = ScriptOpCodes.OP_SHA1;
            break;
        default:
            throw new IllegalArgumentException("unexpected class " + alg);
        }

        LinkedList<byte[]> stack = new LinkedList<>();
        Script.executeScript(null, 0, new ScriptBuilder(s).op(operation).build(), stack, Script.ALL_VERIFY_FLAGS);
        byte[] res = stack.getLast();
        return res;
    }
}
