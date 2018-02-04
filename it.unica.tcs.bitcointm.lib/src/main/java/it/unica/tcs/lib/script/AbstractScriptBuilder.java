/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.lib.script;

import java.io.Serializable;
import java.util.List;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import com.google.common.collect.ImmutableList;

/**
 * Extends BitcoinJ {@link ScriptBuilder} to provide better flexibility.
 * This class overrides some methods <b>in order to return the same class of
 * the provided class {@code T}</b>, which must extends this class.
 * These methods do an explicit cast {@code (T) this}, which is always safe when dealing
 * with concrete classes like {@link ScriptBuilder2}.
 * <p>Examples:</p>
 * <pre>
 * // OK
 * new AbstractScriptBuilder(){}.number(42).number(5).data(new byte[]{})
 * // WRONG: cast exception
 * new AbstractScriptBuilder{@code <ScriptBuilder2>}(){}.number(42).number(5).data(new byte[]{})
 * // OK
 * new ScriptBuilder2().number(42).number(5).data(new byte[]{})
 * </pre>
 *
 * <p>This class is extended by {@code AbstractScriptBuilderWithVar}, that, in turn,
 * is extended by {@code ScriptBuilder2} (which is concrete and public).</p>
 *
 * @param <T> a class extending {@code AbstractScriptBuilder}
 * @see AbstractScriptBuilderWithVar
 * @see ScriptBuilder2
 */
@SuppressWarnings("javadoc")
abstract class AbstractScriptBuilder<T extends AbstractScriptBuilder<T>> extends ScriptBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    protected AbstractScriptBuilder() {
        super();
    }

    protected AbstractScriptBuilder(Script template) {
        super(template);
    }

    public List<ScriptChunk> getChunks() {
        return chunks;
    }

    public int size() {
        return getChunks().size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T data(byte[] data) {
        super.data(data);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T number(long num) {
        super.number(num);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T op(int op) {
        super.op(op);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T opTrue() {
        super.opTrue();
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T opFalse() {
        super.opFalse();
        return (T) this;
    }

    /**
     * Optimize this script builder.
     * Removed the opcodes that don't change the semantic of the resulting script.
     *
     * <table BORDER CELLPADDING=3 CELLSPACING=1>
     * <caption>Examples of optimization</caption>
     *  <tr>
     *    <td ALIGN=CENTER><em>Script</em></td>
     *    <td ALIGN=CENTER><em>Optimized Script</em></td>
     *  </tr>
     *  <tr>
     *    <td><pre>... 42 (TOALTSTACK FROMALTSTACK)* 21 ...</pre></td>
     *    <td><pre>... 42 21 ...</pre></td>
     *  </tr>
     * </table>
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
     * Optimize the given script, returning a copy. A copy is returned even if
     * no optimization can be performed.
     * @param script the script to optimize.
     * @return an optimized script.
     */
    public static Script optimize(Script script) {
        @SuppressWarnings({ "rawtypes", "serial" })
        AbstractScriptBuilder<?> sb = new AbstractScriptBuilder(script) {};
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
        };

        return newValue;
    }

    /*
     * Optimization step
     */
    private static ImmutableList<ScriptChunk> optimize(ImmutableList<ScriptChunk> scriptChunks) {
        if (scriptChunks.size()==0 || scriptChunks.size()==1) {
            return scriptChunks;
        }

        ScriptChunk ch1 = scriptChunks.get(0);
        ScriptChunk ch2 = scriptChunks.get(1);

        if (ch1.equalsOpCode(ScriptOpCodes.OP_TOALTSTACK)
            && ch2.equalsOpCode(ScriptOpCodes.OP_FROMALTSTACK)) {
            return optimize(scriptChunks.subList(2, scriptChunks.size()));
        }

        return ImmutableList.<ScriptChunk>builder().add(scriptChunks.get(0)).addAll(optimize(scriptChunks.subList(1, scriptChunks.size()))).build();
    }
}
