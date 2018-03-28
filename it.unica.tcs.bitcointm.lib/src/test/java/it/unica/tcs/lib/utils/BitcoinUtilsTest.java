/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.lib.utils;

import static org.junit.Assert.assertArrayEquals;

import java.util.LinkedList;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.junit.Test;

import it.unica.tcs.lib.Hash;
import it.unica.tcs.lib.Hash.HashAlgorithm;

public class BitcoinUtilsTest {

    @Test
    public void test_hash_refl() {
        HashAlgorithm[] hashClasses = new HashAlgorithm[]{HashAlgorithm.HASH160,  HashAlgorithm.HASH256, HashAlgorithm.RIPEMD160, HashAlgorithm.SHA256, HashAlgorithm.SHA1};
        Object[] values = new Object[]{1, 1L,"",true,new byte[42], new Hash(new byte[20]), new Hash(new byte[20]), new Hash(new byte[32]), new Hash(new byte[32])};

        for (HashAlgorithm hashCls : hashClasses) {
            for (Object v : values) {
                    assertArrayEquals(
                        executeScript(v, hashCls),
                        BitcoinUtils.hash(v, hashCls).getBytes()
                    );
            }
        }
    }

    @Test
    public void test_hashes_empty() {
        assertArrayEquals(
                executeScript(new byte[]{}, HashAlgorithm.SHA1),
                BitcoinUtils.sha1(new byte[]{}).getBytes()
        );
        assertArrayEquals(
                executeScript(new byte[]{}, HashAlgorithm.RIPEMD160),
                BitcoinUtils.ripemd160(new byte[]{}).getBytes()
        );
        assertArrayEquals(
                executeScript(new byte[]{}, HashAlgorithm.SHA256),
                BitcoinUtils.sha256(new byte[]{}).getBytes()
        );
        assertArrayEquals(
                executeScript(new byte[]{}, HashAlgorithm.HASH160),
                BitcoinUtils.hash160(new byte[]{}).getBytes()
        );
        assertArrayEquals(
                executeScript(new byte[]{}, HashAlgorithm.HASH256),
                BitcoinUtils.hash256(new byte[]{}).getBytes()
        );
        assertArrayEquals(
                executeScript(new byte[]{}, HashAlgorithm.HASH256),
                BitcoinUtils.sha256(BitcoinUtils.sha256(new byte[]{})).getBytes()
        );
        assertArrayEquals(
                executeScript(new byte[]{}, HashAlgorithm.HASH160),
                BitcoinUtils.ripemd160(BitcoinUtils.sha256(new byte[]{})).getBytes()
        );
        assertArrayEquals(
                executeScript(executeScript(new byte[]{}, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
                BitcoinUtils.hash256(new byte[]{}).getBytes()
        );
        assertArrayEquals(
                executeScript(executeScript(new byte[]{}, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
                BitcoinUtils.hash160(new byte[]{}).getBytes()
        );
    }

    @Test
    public void test_hashes_boolean() {
        assertArrayEquals(
                executeScript(true, HashAlgorithm.SHA1),
                BitcoinUtils.sha1(true).getBytes()
        );
        assertArrayEquals(
                executeScript(true, HashAlgorithm.RIPEMD160),
                BitcoinUtils.ripemd160(true).getBytes()
        );
        assertArrayEquals(
                executeScript(true, HashAlgorithm.SHA256),
                BitcoinUtils.sha256(true).getBytes()
        );
        assertArrayEquals(
                executeScript(true, HashAlgorithm.HASH160),
                BitcoinUtils.hash160(true).getBytes()
        );
        assertArrayEquals(
                executeScript(true, HashAlgorithm.HASH256),
                BitcoinUtils.hash256(true).getBytes()
        );
        assertArrayEquals(
                executeScript(true, HashAlgorithm.HASH256),
                BitcoinUtils.sha256(BitcoinUtils.sha256(true)).getBytes()
        );
        assertArrayEquals(
                executeScript(true, HashAlgorithm.HASH160),
                BitcoinUtils.ripemd160(BitcoinUtils.sha256(true)).getBytes()
        );
        assertArrayEquals(
                executeScript(executeScript(true, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
                BitcoinUtils.hash256(true).getBytes()
        );
        assertArrayEquals(
                executeScript(executeScript(true, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
                BitcoinUtils.hash160(true).getBytes()
        );


        assertArrayEquals(
                executeScript(false, HashAlgorithm.SHA1),
                BitcoinUtils.sha1(false).getBytes()
        );
        assertArrayEquals(
                executeScript(false, HashAlgorithm.RIPEMD160),
                BitcoinUtils.ripemd160(false).getBytes()
        );
        assertArrayEquals(
                executeScript(false, HashAlgorithm.SHA256),
                BitcoinUtils.sha256(false).getBytes()
        );
        assertArrayEquals(
                executeScript(false, HashAlgorithm.HASH160),
                BitcoinUtils.hash160(false).getBytes()
        );
        assertArrayEquals(
                executeScript(false, HashAlgorithm.HASH256),
                BitcoinUtils.hash256(false).getBytes()
        );
        assertArrayEquals(
                executeScript(false, HashAlgorithm.HASH256),
                BitcoinUtils.sha256(BitcoinUtils.sha256(false)).getBytes()
        );
        assertArrayEquals(
                executeScript(false, HashAlgorithm.HASH160),
                BitcoinUtils.ripemd160(BitcoinUtils.sha256(false)).getBytes()
        );
        assertArrayEquals(
                executeScript(executeScript(false, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
                BitcoinUtils.hash256(false).getBytes()
        );
        assertArrayEquals(
                executeScript(executeScript(false, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
                BitcoinUtils.hash160(false).getBytes()
        );
    }

    @Test
    public void test_hashes_integers() {
        for (long i=-100; i<100; i++) {
            assertArrayEquals(
                    executeScript(i, HashAlgorithm.SHA1),
                    BitcoinUtils.sha1(i).getBytes()
            );
            assertArrayEquals(
                    executeScript(i, HashAlgorithm.RIPEMD160),
                    BitcoinUtils.ripemd160(i).getBytes()
            );
            assertArrayEquals(
                    executeScript(i, HashAlgorithm.SHA256),
                    BitcoinUtils.sha256(i).getBytes()
            );
            assertArrayEquals(
                    executeScript(i, HashAlgorithm.HASH160),
                    BitcoinUtils.hash160(i).getBytes()
            );
            assertArrayEquals(
                    executeScript(i, HashAlgorithm.HASH256),
                    BitcoinUtils.hash256(i).getBytes()
            );
            assertArrayEquals(
                    executeScript(i, HashAlgorithm.HASH256),
                    BitcoinUtils.sha256(BitcoinUtils.sha256(i)).getBytes()
            );
            assertArrayEquals(
                    executeScript(i, HashAlgorithm.HASH160),
                    BitcoinUtils.ripemd160(BitcoinUtils.sha256(i)).getBytes()
            );
            assertArrayEquals(
                    executeScript(executeScript(i, HashAlgorithm.SHA256), HashAlgorithm.SHA256),
                    BitcoinUtils.hash256(i).getBytes()
            );
            assertArrayEquals(
                    executeScript(executeScript(i, HashAlgorithm.SHA256), HashAlgorithm.RIPEMD160),
                    BitcoinUtils.hash160(i).getBytes()
            );
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
            default: throw new IllegalArgumentException("unexpected class "+alg);
        }
        
        LinkedList<byte[]> stack = new LinkedList<>();
        Script.executeScript(null, 0, new ScriptBuilder(s).op(operation).build(), stack, Script.ALL_VERIFY_FLAGS);
        byte[] res = stack.getLast();
        return res;
    }

}
