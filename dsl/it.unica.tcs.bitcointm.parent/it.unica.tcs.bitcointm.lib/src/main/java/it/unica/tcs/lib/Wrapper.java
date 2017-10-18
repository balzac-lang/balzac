package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;



public interface Wrapper<T> extends Serializable {

	public T get();
	
	public interface NetworkParametersWrapper extends Wrapper<NetworkParameters> {
		public static NetworkParametersWrapper wrap(NetworkParameters obj) {
			final boolean isTest = obj instanceof TestNet3Params;
			checkState(isTest || obj instanceof MainNetParams);
			return new NetworkParametersWrapper() {
				@Override
				public NetworkParameters get() {
					return isTest? TestNet3Params.get(): MainNetParams.get();
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
