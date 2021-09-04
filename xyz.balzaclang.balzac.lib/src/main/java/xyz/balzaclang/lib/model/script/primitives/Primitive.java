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
package xyz.balzaclang.lib.model.script.primitives;

public interface Primitive<T> {

    public T getValue();

    record Number(java.lang.Number value) { }
    record String(java.lang.String value) { }
    record Boolean(java.lang.Boolean value) { }
    record Hash(xyz.balzaclang.lib.model.Hash value) { }
    record Signature(xyz.balzaclang.lib.model.Signature value) { }

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
}
