package it.unica.tcs.xsemantics.interpreter;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import it.unica.tcs.lib.Hash;
import it.unica.tcs.lib.ITransactionBuilder;

public class PlaceholderUtils {
    public static final long INT = 0L;
    public static final String STRING = "";
    public static final boolean BOOLEAN = false;
    public static final Hash HASH = new Hash(new byte[0]);
    public static final Signature SIGNATURE = new Signature(new byte[0]);

    public static PrivateKey KEY(NetworkParameters params) {
        return PrivateKey.fresh(params);
    }

    public static PublicKey PUBKEY(NetworkParameters params) {
        return PublicKey.fresh(params);
    }

    public static Address ADDRESS(NetworkParameters params) {
        return Address.fresh(params);
    }

    public static ITransactionBuilder TX(NetworkParameters params) {
        if (params.equals(MainNetParams.get())) {
            return ITransactionBuilder.fromSerializedTransaction(params, "01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0704ffff001d0104ffffffff0100f2052a0100000043410496b538e853519c726a2c91e61ec11600ae1390813a627c66fb8be7947be63c52da7589379515d4e0a604f8141781e62294721166bf621e73a82cbf2342c858eeac00000000"); 
        }
        else {
            return ITransactionBuilder.fromSerializedTransaction(params, "01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0e0432e7494d010e062f503253482fffffffff0100f2052a010000002321038a7f6ef1c8ca0c588aa53fa860128077c9e6c11e6830f4d7ee4e763a56b7718fac00000000");
        }
    }
}
