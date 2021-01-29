/*
 * Copyright 2020 Nicola Atzei
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
package xyz.balzaclang.lib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class EnvTest {

    @Test
    public void test_empty() {
        Env<Object> env = new Env<Object>();
        String name = "any";

        assertFalse(env.hasVariable(name));
        assertTrue(env.isReady());
        assertTrue(env.getVariables().isEmpty());
        assertTrue(env.getFreeVariables().isEmpty());
        assertTrue(env.getBoundVariables().isEmpty());

        try {
            env.isFree(name);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            env.isBound(name);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            env.getValue(name);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            env.getType(name);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            env.bindVariable(name, 10);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test_add() {
        Env<Object> env = new Env<Object>();
        String name = "any";

        env.addVariable(name, Integer.class);
        env.addVariable(name, Integer.class); // multi add with the same time is fine

        assertTrue(env.hasVariable(name));
        assertTrue(env.isFree(name));
        assertFalse(env.isBound(name));

        assertFalse(env.isReady());

        assertEquals(1, env.getVariables().size());
        assertEquals(1, env.getFreeVariables().size());
        assertEquals(0, env.getBoundVariables().size());

        assertEquals(Integer.class, env.getType(name));

        try {
            // fails if try to set another type
            env.addVariable(name, String.class);
            fail();
        } catch (IllegalArgumentException e) {
        }

        env.bindVariable(name, 10);

        assertTrue(env.hasVariable(name));
        assertFalse(env.isFree(name));
        assertTrue(env.isBound(name));

        assertTrue(env.isReady());

        assertEquals(1, env.getVariables().size());
        assertEquals(0, env.getFreeVariables().size());
        assertEquals(1, env.getBoundVariables().size());

        try {
            // fails if try to set another type
            env.bindVariable(name, 42);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test_clear() {
        Env<Object> env = new Env<Object>();
        String name = "any";
        String name2 = "any2";

        env.addVariable(name, Integer.class);
        env.addVariable(name2, Long.class); // multi add with the same time is fine

        assertFalse(env.isReady());
        assertEquals(2, env.getVariables().size());
        assertEquals(2, env.getFreeVariables().size());
        assertEquals(0, env.getBoundVariables().size());

        env.bindVariable(name, 5);
//      env.bindVariable(name2, 8745287489874L);
        System.out.println(env);

        env.clear();

        assertTrue(env.isReady());
        assertEquals(0, env.getVariables().size());
        assertEquals(0, env.getFreeVariables().size());
        assertEquals(0, env.getBoundVariables().size());
    }

    @Test
    public void test_type() {
        Env<Number> env = new Env<Number>();

        env.addVariable("a", Integer.class);
        env.addVariable("b", Long.class);
        env.addVariable("c", Float.class);

        assertEquals(Integer.class, env.getType("a"));
        assertEquals(Long.class, env.getType("b"));
        assertEquals(Float.class, env.getType("c"));
    }

    @Test
    public void test_getValue() {
        Env<Number> env = new Env<Number>();

        env.addVariable("a", Integer.class);

        assertEquals(10, env.getValueOrDefault("a", 10));

        env.bindVariable("a", 5);

        assertEquals(5, env.getValue("a"));
        assertEquals(5, env.getValueOrDefault("a", 10));

        assertEquals(Integer.class, env.getValue("a", Integer.class).getClass());
        try {
            env.getValue("a", Long.class);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test_removeVariable() {
        Env<Number> env = new Env<Number>();

        env.addVariable("a", Integer.class);

        assertTrue(env.hasVariable("a"));

        env.removeVariable("a");

        assertFalse(env.hasVariable("a"));
    }

    @Test
    public void test_equals() {
        Env<Number> env1 = new Env<Number>();
        Env<Integer> env2 = new Env<Integer>();
        assertFalse(env1.equals(null)); // null
        assertTrue(env1.equals(env1)); // same obj
        assertFalse(env1.equals(5)); // different class

        assertTrue(env1.equals(env2));
        assertTrue(env1.hashCode() == env2.hashCode());

        env1.addVariable("a", Integer.class);
        assertFalse(env1.equals(env2)); // env1 has variable "a"
        assertFalse(env1.hashCode() == env2.hashCode());

        env2.addVariable("a", Integer.class);
        assertTrue(env1.equals(env2)); // both have variable "a"
        assertTrue(env1.hashCode() == env2.hashCode());

        env1.bindVariable("a", 42);
        assertFalse(env1.equals(env2)); // "a" is bound in env1
        assertFalse(env1.hashCode() == env2.hashCode());

        env1.addVariable("b", Long.class);
        env2.addVariable("b", Integer.class);

        assertFalse(env1.equals(env2)); // "b" has different type
        assertFalse(env1.hashCode() == env2.hashCode());
    }
}
