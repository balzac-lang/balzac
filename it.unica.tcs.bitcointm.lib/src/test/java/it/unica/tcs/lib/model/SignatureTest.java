/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static org.junit.Assert.assertArrayEquals;
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
	public void testPubkeyImmutability() {
		byte[] sigbytes = new byte[]{1,2,3,4};
		byte[] pubkey = new byte[] {5,6,7,8};
		Signature sig = new Signature(sigbytes, pubkey);
		
		pubkey[3] = 0;
		assertTrue(sig.getPubkey().isPresent());
		assertFalse("Pubkey must be immutable", Arrays.equals(sig.getPubkey().get(), pubkey));
	}

	@Test
	public void testSetPubkey() {
		byte[] sigbytes = new byte[]{1,2,3,4};
		byte[] pubkey = new byte[] {5,6,7,8};
		Signature sig = new Signature(sigbytes);

		// check the key is absent
		assertFalse(sig.getPubkey().isPresent());

		sig.setPubkey(pubkey);

		// check the key is defined
		assertTrue(sig.getPubkey().isPresent());
		assertArrayEquals(pubkey, sig.getPubkey().get());

		// check the key is immutable
		pubkey[3] = 0;
		assertFalse("Pubkey must be immutable", Arrays.equals(sig.getPubkey().get(), pubkey));

		// reset the key
		sig.setPubkey(null);
		assertFalse(sig.getPubkey().isPresent());
	}

	@Test
	public void testEquality() {
		// two signature are equals despite their public keys
		Signature sigA = new Signature(new byte[] {1,2,3,4});
		Signature sigB = new Signature(new byte[] {1,2,3,4}, new byte[] {5,6,7,8});

		assertTrue(sigA.equals(sigB));
		assertTrue(sigA.hashCode() == sigB.hashCode());
	}
}
