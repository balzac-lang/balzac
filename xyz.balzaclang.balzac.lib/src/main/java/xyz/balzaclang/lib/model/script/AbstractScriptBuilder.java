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

import java.io.Serializable;
import java.util.List;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import com.google.common.collect.ImmutableList;

/**
 * Extends BitcoinJ {@link ScriptBuilder} to provide better flexibility. This
 * class overrides some methods <b>in order to return the same class of the
 * provided class {@code T}</b>, which must extends this class. These methods do
 * an explicit cast {@code (T) this}, which is always safe when dealing with
 * concrete classes like {@link ScriptBuilder2}.
 * <p>
 * Examples:
 * </p>
 * 
 * <pre>
 * // OK
 * new AbstractScriptBuilder(){}.number(42).number(5).data(new byte[]{})
 * // WRONG: cast exception
 * new AbstractScriptBuilder{@code <ScriptBuilder2>}(){}.number(42).number(5).data(new byte[]{})
 * // OK
 * new ScriptBuilder2().number(42).number(5).data(new byte[]{})
 * </pre>
 *
 * <p>
 * This class is extended by {@code ScriptBuilderWithVar}, that, in turn, is
 * extended by {@code ScriptBuilder2} (which is concrete and public).
 * </p>
 *
 * @param <T> a class extending {@code AbstractScriptBuilder}
 * @see ScriptBuilderWithVar
 * @see ScriptBuilder2
 */
@SuppressWarnings("javadoc")
public abstract class AbstractScriptBuilder<T extends AbstractScriptBuilder<T>> implements Serializable {

    private ScriptBuilder sb;

    private static final long serialVersionUID = 1L;

    protected AbstractScriptBuilder() {
        sb = new ScriptBuilder();
    }

    protected AbstractScriptBuilder(Script template) {
        sb = new ScriptBuilder(template);
    }

    public List<ScriptChunk> getChunks() {
        return sb.getChunks();
    }

    @SuppressWarnings("unchecked")
    public T addChunk(ScriptChunk chunk) {
        sb.addChunk(chunk);
        return (T) this;
    }

    public int size() {
        return getChunks().size();
    }

    @SuppressWarnings("unchecked")
    public T data(byte[] data) {
        sb.data(data);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T number(long num) {
        sb.number(num);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T op(int op) {
        sb.op(op);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T opTrue() {
        sb.opTrue();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T opFalse() {
        sb.opFalse();
        return (T) this;
    }

    public Script build() {
        return sb.build();
    }

    /**
     * Optimize this script builder. Removed the opcodes that don't change the
     * semantic of the resulting script.
     *
     * <table BORDER CELLPADDING=3 CELLSPACING=1> <caption>Examples of
     * optimization</caption>
     * <tr>
     * <td ALIGN=CENTER><em>Script</em></td>
     * <td ALIGN=CENTER><em>Optimized Script</em></td>
     * </tr>
     * <tr>
     * <td>
     * 
     * <pre>
     * ... 42 (TOALTSTACK FROMALTSTACK)* 21 ...
     * </pre>
     * 
     * </td>
     * <td>
     * 
     * <pre>
     * ... 42 21 ...
     * </pre>
     * 
     * </td>
     * </tr>
     * </table>
     * 
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    public T optimize() {
        List<ScriptChunk> chs = iterateOptimize(ImmutableList.copyOf(getChunks()));
        this.getChunks().clear();
        this.getChunks().addAll(chs);
        return (T) this;
    }

    /**
     * Optimize the given script, returning a copy. A copy is returned even if no
     * optimization can be performed.
     * 
     * @param script the script to optimize.
     * @return an optimized script.
     */
    public static Script optimize(Script script) {
        @SuppressWarnings({ "rawtypes", "serial" })
        AbstractScriptBuilder<?> sb = new AbstractScriptBuilder(script) {
        };
        return sb.optimize().build();
    }

    /*
     * Repeat the optimization step
     */
    private static ImmutableList<ScriptChunk> iterateOptimize(ImmutableList<ScriptChunk> scriptChunks) {
        ImmutableList<ScriptChunk> value = scriptChunks;
        ImmutableList<ScriptChunk> newValue = optimize(scriptChunks);

        while (!newValue.equals(value)) {
            value = newValue;
            newValue = optimize(value);
        }
        ;

        return newValue;
    }

    /*
     * Optimization step
     */
    private static ImmutableList<ScriptChunk> optimize(ImmutableList<ScriptChunk> scriptChunks) {
        if (scriptChunks.size() == 0 || scriptChunks.size() == 1) {
            return scriptChunks;
        }

        ScriptChunk ch1 = scriptChunks.get(0);
        ScriptChunk ch2 = scriptChunks.get(1);

        if (ch1.equalsOpCode(ScriptOpCodes.OP_TOALTSTACK) && ch2.equalsOpCode(ScriptOpCodes.OP_FROMALTSTACK)) {
            return optimize(scriptChunks.subList(2, scriptChunks.size()));
        }

        return ImmutableList.<ScriptChunk>builder().add(scriptChunks.get(0))
            .addAll(optimize(scriptChunks.subList(1, scriptChunks.size()))).build();
    }
}
