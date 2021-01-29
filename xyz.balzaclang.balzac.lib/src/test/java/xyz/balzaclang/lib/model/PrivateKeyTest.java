/*
 * Copyright 2021 Nicola Atzei
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

import static org.junit.Assert.*;

import org.junit.Test;

public class PrivateKeyTest {

    private NetworkType networkType = NetworkType.TESTNET;

    @Test
    public void testCompressedKeysEquality() {

        byte[] bytes = new byte[] { 1, 2, 3, 4 };

        PrivateKey k1 = PrivateKey.from(bytes, true, networkType);
        PrivateKey k2 = PrivateKey.from(bytes, false, networkType);

        assertTrue(k1.equals(k2));
        assertFalse(k1.toPublicKey().equals(k2.toPublicKey()));
        assertFalse(k1.toAddress().equals(k2.toAddress()));

        assertEquals("cMahea7zqjxrtgAbB7LSGbcQUr1uX1ojuat9jZodN7H9U97a6MPk", k1.getWif());
        assertEquals("91avARGdfge8E4tZfYLoxeJ5sGBdNJQH4kvjJoQFanNnRJd2keY", k2.getWif());

        assertEquals("mnmhLRXDxpNzA6Wr99wZNRQcQWzQk2uRs5", k1.toAddress().getWif());
        assertEquals("mkRANnsZDHb6cp1uUGvdNQQaDCToDKRW9Q", k2.toAddress().getWif());
    }
}
