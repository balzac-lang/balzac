package it.unica.tcs.utils;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

public class SignatureAndKey {
    private final TransactionSignature sig;

    private final ECKey pubkey;

    public SignatureAndKey(final TransactionSignature sig, final ECKey pubkey) {
        super();
        this.sig = sig;
        this.pubkey = pubkey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.sig == null) ? 0 : this.sig.hashCode());
        result = prime * result + ((this.pubkey == null) ? 0 : this.pubkey.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SignatureAndKey other = (SignatureAndKey) obj;
        if (this.sig == null) {
            if (other.sig != null)
                return false;
        } else if (!this.sig.equals(other.sig))
            return false;
        if (this.pubkey == null) {
            if (other.pubkey != null)
                return false;
        } else if (!this.pubkey.equals(other.pubkey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("sig", this.sig);
        b.add("pubkey", this.pubkey);
        return b.toString();
    }

    public TransactionSignature getSig() {
        return this.sig;
    }

    public ECKey getPubkey() {
        return this.pubkey;
    }
}
