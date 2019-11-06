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
package xyz.balzaclang.lib.model.script;

import static org.bitcoinj.script.ScriptOpCodes.OP_16;
import static org.bitcoinj.script.ScriptOpCodes.OP_4;
import static org.bitcoinj.script.ScriptOpCodes.OP_FROMALTSTACK;
import static org.bitcoinj.script.ScriptOpCodes.OP_TOALTSTACK;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.junit.Test;

import xyz.balzaclang.lib.model.script.AbstractScriptBuilder;
import xyz.balzaclang.lib.model.script.ScriptBuilderWithVar;

public class AbstractScriptBuilderTest {

    @Test
    public void test_size() {
        @SuppressWarnings({ "serial", "rawtypes" })
        AbstractScriptBuilder<?> sb = new AbstractScriptBuilder() {};
        assertEquals(0, sb.size());
        sb.number(42);
        assertEquals(1, sb.size());      // PUSHDATA
        sb.number(5);
        assertEquals(2, sb.size());      // OP_5
    }

    @Test
    public void test_chain_of_instruction() {
        @SuppressWarnings({ "serial", "rawtypes" })
        AbstractScriptBuilder<?> sb = new AbstractScriptBuilder() {};
        assertEquals(0, sb.size());
        sb.number(42).number(5).data(new byte[]{});
        assertEquals(3, sb.size());
    }

    @Test
    public void test_overridden_methods() {
        @SuppressWarnings({ "serial", "rawtypes" })
        AbstractScriptBuilder<?> sb = new AbstractScriptBuilder() {};
        assertTrue(sb.data(new byte[]{}) instanceof AbstractScriptBuilder);
        assertTrue(sb.number(42) instanceof AbstractScriptBuilder);
        assertTrue(sb.op(OP_16) instanceof AbstractScriptBuilder);
        assertTrue(sb.opTrue() instanceof AbstractScriptBuilder);
        assertTrue(sb.opFalse() instanceof AbstractScriptBuilder);
    }

    @Test
    public void test_optimize() {
        Script s = new ScriptBuilder()
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .build();

        Script opt = AbstractScriptBuilder.optimize(s);

        assertTrue(s!=opt);
        assertEquals(2, s.getChunks().size());
        assertEquals(0, opt.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_FROMALTSTACK});
        assertArrayEquals(opt.getProgram(), new byte[]{});
    }

    @Test
    public void test_optimize2() {
        Script s = new ScriptBuilder()
                .op(OP_TOALTSTACK)
                .number(4)
                .op(OP_FROMALTSTACK)
                .build();

        Script opt = AbstractScriptBuilder.optimize(s);

        assertTrue(s!=opt);
        assertEquals(3, s.getChunks().size());
        assertEquals(3, opt.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_4, OP_FROMALTSTACK});
        assertArrayEquals(s.getProgram(), opt.getProgram());
    }

    @Test
    public void test_optimize3() {
        Script s = new ScriptBuilder()
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .number(4)
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .build();

        Script opt = AbstractScriptBuilder.optimize(s);

        assertTrue(s!=opt);
        assertEquals(5, s.getChunks().size());
        assertEquals(1, opt.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_FROMALTSTACK, OP_4, OP_TOALTSTACK, OP_FROMALTSTACK});
        assertArrayEquals(opt.getProgram(), new byte[]{OP_4});
    }

    @Test
    public void test_optimize4() {
        Script s = new ScriptBuilder()
                .op(OP_TOALTSTACK)
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .op(OP_FROMALTSTACK)
                .number(4)
                .build();

        Script opt = AbstractScriptBuilder.optimize(s);

        assertTrue(s!=opt);
        assertEquals(5, s.getChunks().size());
        assertEquals(1, opt.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_TOALTSTACK, OP_FROMALTSTACK, OP_FROMALTSTACK, OP_4});
        assertArrayEquals(opt.getProgram(), new byte[]{OP_4});
    }

    @Test
    public void test_optimize5() {
        Script s = new ScriptBuilder()
                .op(OP_TOALTSTACK)
                .op(OP_TOALTSTACK)
                .number(4)
                .op(OP_FROMALTSTACK)
                .op(OP_FROMALTSTACK)
                .build();

        Script opt = AbstractScriptBuilder.optimize(s);

        assertTrue(s!=opt);
        assertEquals(5, s.getChunks().size());
        assertEquals(5, opt.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_TOALTSTACK, OP_4, OP_FROMALTSTACK, OP_FROMALTSTACK});
        assertArrayEquals(s.getProgram(), opt.getProgram());
    }

    @Test
    public void test_optimize_sb() {
        Script s = new ScriptBuilderWithVar<>()
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .optimize()
                .build();

        assertEquals(0, s.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{});
    }

    @Test
    public void test_optimize2_sb() {
        Script s = new ScriptBuilderWithVar<>()
                .op(OP_TOALTSTACK)
                .number(4)
                .op(OP_FROMALTSTACK)
                .optimize()
                .build();

        assertEquals(3, s.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_4, OP_FROMALTSTACK});
    }

    @Test
    public void test_optimize3_sb() {
        Script s = new ScriptBuilderWithVar<>()
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .number(4)
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .optimize()
                .build();

        assertEquals(1, s.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_4});
    }

    @Test
    public void test_optimize4_sb() {
        Script s = new ScriptBuilderWithVar<>()
                .op(OP_TOALTSTACK)
                .op(OP_TOALTSTACK)
                .op(OP_FROMALTSTACK)
                .op(OP_FROMALTSTACK)
                .number(4)
                .optimize()
                .build();

        assertEquals(1, s.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_4});
    }

    @Test
    public void test_optimize5_sb() {
        Script s = new ScriptBuilderWithVar<>()
                .op(OP_TOALTSTACK)
                .op(OP_TOALTSTACK)
                .number(4)
                .op(OP_FROMALTSTACK)
                .op(OP_FROMALTSTACK)
                .optimize()
                .build();

        assertEquals(5, s.getChunks().size());
        assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_TOALTSTACK, OP_4, OP_FROMALTSTACK, OP_FROMALTSTACK});
    }
}
