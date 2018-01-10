/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.lib;

import static org.junit.Assert.*;

import org.junit.Test;

import it.unica.tcs.lib.utils.BitcoinUtils;

public class HashTest {

    private static final String BYTES_160 = "cd98bf0202ef07e38e87f6bd9445e5e7331e2c78";
    private static final String BYTES_256 = "2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b";

    private static byte[] bytes160 = BitcoinUtils.decode(BYTES_160);
    private static byte[] bytes256 = BitcoinUtils.decode(BYTES_256);

    @Test
    public void test_getType() {
        assertEquals("ripemd160", new Hash.Ripemd160(bytes160).getType());
        assertEquals("hash160", new Hash.Hash160(bytes160).getType());
        assertEquals("sha256", new Hash.Sha256(bytes256).getType());
        assertEquals("hash256", new Hash.Hash256(bytes256).getType());
    }
    
    @Test
    public void test_getBytes() {
        assertEquals(bytes160, new Hash.Ripemd160(bytes160).getBytes());
        assertEquals(bytes160, new Hash.Hash160(bytes160).getBytes());
        assertEquals(bytes256, new Hash.Sha256(bytes256).getBytes());
        assertEquals(bytes256, new Hash.Hash256(bytes256).getBytes());
    }

    @Test
    public void test_bytesAsString() {
        assertEquals(BYTES_160, new Hash.Ripemd160(bytes160).getBytesAsString());
        assertEquals(BYTES_160, new Hash.Hash160(bytes160).getBytesAsString());
        assertEquals(BYTES_256, new Hash.Sha256(bytes256).getBytesAsString());
        assertEquals(BYTES_256, new Hash.Hash256(bytes256).getBytesAsString());
    }

    @Test
    public void test_invalidConstructor_Ripemd() {
        try {
            new Hash.Ripemd160(bytes256);
            fail();
        }
        catch (IllegalArgumentException e) {}
    }

    @Test
    public void test_invalidConstructor_Hash160() {
        try {
            new Hash.Hash160(bytes256);
            fail();
        }
        catch (IllegalArgumentException e) {}
    }

    @Test
    public void test_invalidConstructor_Sha256() {
        try {
            new Hash.Sha256(bytes160);
            fail();
        }
        catch (IllegalArgumentException e) {}
    }

    @Test
    public void test_invalidConstructor_Hash256() {
        try {
            new Hash.Hash256(bytes160);
            fail();
        }
        catch (IllegalArgumentException e) {}
    }
    
    @Test
    public void test_equals() {
        // same bytes, type must be any Hash
        Hash h1 = new Hash.Ripemd160(bytes160);
        Hash h2 = new Hash.Hash160(bytes160);
        Hash h3 = new Hash.Sha256(bytes256);
        Hash h4 = new Hash.Hash256(bytes256);
        assertTrue(h1.equals(h1));
        assertTrue(h2.equals(h2));
        assertTrue(h3.equals(h3));
        assertTrue(h4.equals(h4));
        assertTrue(h1.equals(h2));
        assertTrue(h3.equals(h4));
        assertFalse(h1.equals(null));
        assertFalse(h2.equals(null));
        assertFalse(h3.equals(null));
        assertFalse(h4.equals(null));
        assertFalse(h1.equals(5));
        assertFalse(h2.equals(5));
        assertFalse(h3.equals(5));
        assertFalse(h4.equals(5));
        assertFalse(h1.equals(h3));
        assertFalse(h2.equals(h4));
    }

    @Test
    public void test_hashCode() {
        // same bytes, type must be any Hash
        Hash h1 = new Hash.Ripemd160(bytes160);
        Hash h2 = new Hash.Hash160(bytes160);
        Hash h3 = new Hash.Sha256(bytes256);
        Hash h4 = new Hash.Hash256(bytes256);
        assertEquals(h1.hashCode(), h2.hashCode());
        assertEquals(h3.hashCode(), h4.hashCode());
    }

    @Test
    public void test_toString() {
        Hash h1 = new Hash.Ripemd160(bytes160);
        Hash h2 = new Hash.Hash160(bytes160);
        Hash h3 = new Hash.Sha256(bytes256);
        Hash h4 = new Hash.Hash256(bytes256);
        assertEquals(h1.getType()+":"+h1.getBytesAsString(), h1.toString());
        assertEquals(h2.getType()+":"+h2.getBytesAsString(), h2.toString());
        assertEquals(h3.getType()+":"+h3.getBytesAsString(), h3.toString());
        assertEquals(h4.getType()+":"+h4.getBytesAsString(), h4.toString());
    }
}
