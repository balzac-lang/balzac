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
package xyz.balzaclang.lib.model.script.primitives;

import xyz.balzaclang.lib.model.transaction.ITransactionBuilder;

public sealed interface Primitive {

    record Number(java.lang.Number value) implements Primitive { }
    record String(java.lang.String value) implements Primitive { }
    record Boolean(java.lang.Boolean value) implements Primitive { }
    record Hash(xyz.balzaclang.lib.model.Hash value) implements Primitive { }
    record Signature(xyz.balzaclang.lib.model.Signature value) implements Primitive { }
    record PrivateKey(xyz.balzaclang.lib.model.PrivateKey value) implements Primitive { }
    record PublicKey(xyz.balzaclang.lib.model.PublicKey value) implements Primitive { }
    record Address(xyz.balzaclang.lib.model.Address value) implements Primitive { }
    record Transaction(ITransactionBuilder value) implements Primitive { }

    public static Number of(java.lang.Number value) {
        return new Number(value);
    }

    public static String of(java.lang.String value) {
        return new String(value);
    }

    public static Boolean of(java.lang.Boolean value) {
        return new Boolean(value);
    }

    public static Hash of(xyz.balzaclang.lib.model.Hash value) {
        return new Hash(value);
    }

    public static Signature of(xyz.balzaclang.lib.model.Signature value) {
        return new Signature(value);
    }

    public static PrivateKey of(xyz.balzaclang.lib.model.PrivateKey value) {
        return new PrivateKey(value);
    }

    public static PublicKey of(xyz.balzaclang.lib.model.PublicKey value) {
        return new PublicKey(value);
    }

    public static Address of(xyz.balzaclang.lib.model.Address value) {
        return new Address(value);
    }

    public static Transaction of(xyz.balzaclang.lib.model.transaction.ITransactionBuilder value) {
        return new Transaction(value);
    }
}
