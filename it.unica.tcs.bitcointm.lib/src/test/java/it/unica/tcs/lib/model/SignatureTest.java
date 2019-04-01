/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class SignatureTest {

	@Test
	public void testCreate() {
		byte[] sigbytes = new byte[]{1,2,3,4};
		Signature sig = new Signature(sigbytes);

		assertTrue(Arrays.equals(sig.getSignature(), sigbytes));
		assertFalse(sig.getPubkey().isPresent());
	}

	@Test
	public void testSigImmutability() {
		byte[] sigbytes = new byte[]{1,2,3,4};
		Signature sig = new Signature(sigbytes);

		sigbytes[3] = 0;
		assertFalse("Signature must be immutable", Arrays.equals(sig.getSignature(), sigbytes));
		assertFalse(sig.getPubkey().isPresent());
	}
	
	@Test
	public void testEquality() {
		// two signature are equals despite their public keys
		Signature sigA = new Signature(new byte[] {1,2,3,4});
		Signature sigB = new Signature(new byte[] {1,2,3,4}, PublicKey.fresh());

		assertTrue(sigA.equals(sigB));
		assertTrue(sigA.hashCode() == sigB.hashCode());
	}
}
