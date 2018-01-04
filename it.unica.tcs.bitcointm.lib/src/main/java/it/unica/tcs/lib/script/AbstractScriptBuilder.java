package it.unica.tcs.lib.script;

import java.io.Serializable;
import java.util.List;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import com.google.common.collect.ImmutableList;

public abstract class AbstractScriptBuilder<T extends AbstractScriptBuilder<T>> extends ScriptBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    public AbstractScriptBuilder() {
        super();
    }

    public AbstractScriptBuilder(Script template) {
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

    protected static Script optimize(Script s) {
        ScriptBuilder sb = new ScriptBuilder();
        List<ScriptChunk> chs = iterateOptimize(ImmutableList.copyOf(s.getChunks()));
        for (ScriptChunk ch : chs)
            sb.addChunk(ch);
        return sb.build();
    }

    @SuppressWarnings("unchecked")
    public T optimize() {
        List<ScriptChunk> chs = iterateOptimize(ImmutableList.copyOf(getChunks()));
        this.getChunks().clear();
        this.getChunks().addAll(chs);
        return (T) this;
    }

    private static ImmutableList<ScriptChunk> iterateOptimize(ImmutableList<ScriptChunk> scriptChunks) {
        ImmutableList<ScriptChunk> value = scriptChunks;
        ImmutableList<ScriptChunk> newValue = optimize(scriptChunks);

        while (!newValue.equals(value)) {
            value = newValue;
            newValue = optimize(value);
        };

        return newValue;
    }

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
