package it.unica.tcs.lib;

import java.io.Serializable;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;



public interface Wrapper<T> extends Serializable {

	public T get();
	
	public static NetworkParametersWrapper MAIN = NetworkParametersWrapper.wrap(MainNetParams.get());
	public static NetworkParametersWrapper TESTNET = NetworkParametersWrapper.wrap(TestNet3Params.get());
	public static NetworkParametersWrapper REGTEST = NetworkParametersWrapper.wrap(RegTestParams.get());
	
	public interface NetworkParametersWrapper extends Wrapper<NetworkParameters> {
		public static NetworkParametersWrapper wrap(NetworkParameters obj) {
			final boolean isTest = obj instanceof TestNet3Params;
			final boolean isMainnet = obj instanceof MainNetParams;
			final boolean isRegtest = obj instanceof RegTestParams;
			return new NetworkParametersWrapper() {
				@Override
				public NetworkParameters get() {
					if (isMainnet) return MainNetParams.get();
					if (isTest) return TestNet3Params.get();
					if (isRegtest) return RegTestParams.get();
					throw new IllegalStateException();
				}
				private static final long serialVersionUID = 1L;
			};
		}
	}
	
	public interface SigHashWrapper extends Wrapper<SigHash> {
		public static SigHashWrapper wrap(SigHash obj) {
			return new SigHashWrapper() {
				@Override
				public SigHash get() {
					return obj;
				}
				private static final long serialVersionUID = 1L;
			};
		}
	}
	
	public static <T> Wrapper<T> wrap(T obj) {
		return new Wrapper<T>() {
			@Override
			public T get() {
				return obj;
			}
			private static final long serialVersionUID = 1L;
		};
	}
}
