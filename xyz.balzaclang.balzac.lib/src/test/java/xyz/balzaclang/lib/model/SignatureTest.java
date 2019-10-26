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
