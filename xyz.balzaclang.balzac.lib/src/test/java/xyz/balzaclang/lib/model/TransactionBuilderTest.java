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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.common.collect.Sets;

import xyz.balzaclang.lib.model.script.InputScript;
import xyz.balzaclang.lib.model.script.OutputScript;
import xyz.balzaclang.lib.utils.ObjectUtils;

public class TransactionBuilderTest {


    @Test
    public void test_coinbase_serialization() {
        CoinbaseTransactionBuilder net = new CoinbaseTransactionBuilder(NetworkType.MAINNET);
        net.addInput(Input.of(InputScript.create()));
        String s2 = ObjectUtils.serializeObjectToStringQuietly(net);
        ITransactionBuilder net2 = ObjectUtils.deserializeObjectFromStringQuietly(s2, ITransactionBuilder.class);

        assertTrue(net.isCoinbase());
        assertTrue(net2.isCoinbase());
    }

    @Test
    public void test_tx_serialization() {
        TransactionBuilder net = new TransactionBuilder(NetworkType.MAINNET);
        net.addInput(Input.of(InputScript.create()));
        String s2 = ObjectUtils.serializeObjectToStringQuietly(net);
        ITransactionBuilder net2 = ObjectUtils.deserializeObjectFromStringQuietly(s2, ITransactionBuilder.class);

        assertTrue(net.isCoinbase());
        assertTrue(net2.isCoinbase());
    }

    @Test
    public void test() {
        TransactionBuilder tb = new TransactionBuilder(NetworkType.MAINNET);
        tb.addVariable("foo", Integer.class);
        tb.addVariable("veryLongName", String.class);
        tb.addVariable("anotherVeryLongName", String.class).bindVariable("anotherVeryLongName", "hihihhihihihihihiihihihi");

        tb.addInput(Input.of(InputScript.create().number(5)));
        tb.addInput(Input.of(InputScript.create().number(42)));
        tb.addOutput(OutputScript.createP2SH().number(3).addVariable("veryLongName", String.class).addVariable("foo", String.class).bindVariable("foo", "pippo"), 34);

        System.out.println(tb);
    }


    @Test
    public void test_addInput() {
        TransactionBuilder tb = new TransactionBuilder(NetworkType.MAINNET);
        tb.addVariable("foo", Integer.class);

        Input in = Input.of(InputScript.create().number(5).addVariable("foo", String.class), 34);

        try {
            tb.addInput(in);
            fail();
        }
        catch (IllegalArgumentException e){}
    }

    @Test
    public void test_hook() {

        TransactionBuilder tb = new TransactionBuilder(NetworkType.MAINNET);

        tb.addVariable("a", Integer.class);
        tb.addVariable("b", String.class);

        MutableBoolean hook_A = new MutableBoolean();
        MutableBoolean hook_B = new MutableBoolean();
        MutableBoolean hook_A_B = new MutableBoolean();

        tb.addHookToVariableBinding(Sets.newHashSet("a"), values -> {
            System.out.println("executing hook A");
            assertEquals(1,values.size());
            assertEquals(42,values.get("a"));
            hook_A.value = true;
        });

        tb.addHookToVariableBinding(Sets.newHashSet("b"), values -> {
            System.out.println("executing hook B");
            assertEquals(1,values.size());
            assertEquals("hello",values.get("b"));
            hook_B.value = true;
        });

        tb.addHookToVariableBinding(Sets.newHashSet("a","b"), values -> {
            System.out.println("executing hook A_B");
            assertEquals(2,values.size());
            assertEquals(42,values.get("a"));
            assertEquals("hello",values.get("b"));
            hook_A_B.value = true;
        });

        try {
            tb.addHookToVariableBinding(Sets.newHashSet("b","a"), values -> {});
            fail();
        }
        catch (IllegalArgumentException e) {}

        assertTrue(tb.hasHook("a")==true);
        assertTrue(tb.hasHook("b")==true);
        assertTrue(tb.hasHook("a","b")==true);
        assertTrue(tb.hasHook("b","a")==true);
        assertTrue(tb.hasHook("c")==false);
        assertTrue(hook_A.value==false);
        assertTrue(hook_B.value==false);
        assertTrue(hook_A_B.value==false);

        System.out.println("binding A");
        tb.bindVariable("a", 42);

        assertTrue(tb.hasHook("a")==false);
        assertTrue(tb.hasHook("b")==true);
        assertTrue(tb.hasHook("a","b")==true);
        assertTrue(tb.hasHook("b","a")==true);
        assertTrue(hook_A.value==true);
        assertTrue(hook_B.value==false);
        assertTrue(hook_A_B.value==false);

        System.out.println("binding B");
        tb.bindVariable("b", "hello");

        assertTrue(tb.hasHook("a")==false);
        assertTrue(tb.hasHook("b")==false);
        assertTrue(tb.hasHook("a","b")==false);
        assertTrue(tb.hasHook("b","a")==false);
        assertTrue(hook_A.value==true);
        assertTrue(hook_B.value==true);
        assertTrue(hook_A_B.value==true);
    }

    class MutableBoolean {
        boolean value;
    }
}
