/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.lib;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unica.tcs.lib.model.Hash;

public class TestHash {

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
}
