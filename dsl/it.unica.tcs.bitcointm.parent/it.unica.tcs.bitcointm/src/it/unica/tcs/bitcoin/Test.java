package it.unica.tcs.bitcoin;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;


public class Test {
	
	float a = 1.5_000f; 
	int b = 0b0000_0000__0100_0000__0000_0000__0000_0000;
	
	public static void main(String[] args) {
		
		
		String[] inputs = new String[]{
				// valid
				"1days",   "1 days",   "1              days",
				"1months", "1 months", "1              months",
				};

		String patternS = 
				  "("
			    + "(?<days>\\d([\\d_]*[\\d]+)?)\\s*days\\s*"
				+ "(?<months>\\d([\\d_]*[\\d]+)?)\\s*months\\s*"
				+ ")|"
				+ "((?<blocks>\\d([\\d_]*[\\d]+)?)\\s*blocks\\s*)";

		Pattern pattern = Pattern.compile(patternS);
		
		for (String i : inputs) {
			
			Matcher matcher = pattern.matcher(i);
			
			System.out.println("input: "+i);
			if (matcher.matches()) {
				System.out.println("days: "+matcher.group("days"));
				System.out.println("months: "+matcher.group("months"));
				System.out.println("blocks: "+matcher.group("blocks"));
			}
			else {
				System.out.println("no match found");
			}
			System.out.println();
		}
		
		
		//keyTest();
		//issueWiFRepresentation();
		//deserializeTx();
		//hexTest();
	}
	
	
	@SuppressWarnings("unused")
	private static void hexTest() {
		
		String[] inputs = new String[]{
				// valid
				"5",
				"10", 
				"10_000",
				"10.5",
				"10_000.5_000",
				"0.5 BTC", 
				"0x42", 
				"0X45",
				"0x42_000", 
				"0X45_000",
				"5 BTC",
				"5                 BTC",
				"0.5               BTC",
				// invalid
				"",			// empty number
				"_5",		// preceded by _
				"5_",		// followed by _
				"_5_",		// sorrounded by_
				"_10",
				"10_",
				"10 _000",
				"10 .5_",
				"_10_000.5_000",
				};
		String patternS = 
				"((?<intpart>\\d([\\d_]*[\\d]+)?)(\\.(?<decpart>\\d([\\d_]*[\\d]+)?))?(\\s*(?<btcpart>BTC)?))"
				+ "|"
				+ "(?<hexpart>(0x|0X)[\\dA-Fa-f][\\dA-Fa-f_]*[\\dA-Fa-f]+)";

		Pattern pattern = Pattern.compile(patternS);
		
		for (String i : inputs) {
			
			Matcher matcher = pattern.matcher(i);
			
			System.out.println("input: "+i);
			if (matcher.matches()) {
				System.out.println("intpart: "+matcher.group("intpart"));
				System.out.println("decpart: "+matcher.group("decpart"));
				System.out.println("hexpart: "+matcher.group("hexpart"));
				System.out.println("btcpart: "+matcher.group("btcpart"));
			}
			else {
				System.out.println("no match found");
			}
			System.out.println();
		}
	}
	@SuppressWarnings("unused")
	private static void deserializeTx() {
		NetworkParameters params = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
		
		String txString = "01000000026d95842f4f81132c7ee30b4254291a4f5497dac5af29a317881e9d0d70ee9c8d0c000000fc0047304402201e0becd9b38eb70925169786a6d8ffac13f11e9df5a3fee1e6a5ada1052eb4f40220438d66b2204d74fc56dc97ec38aeaca94fcdbc8d563df2dee1530cfd7dcb4abf0147304402203f5364cb627e95d3790b4605a9e9f1f2ff2e7fd539a21d929474e3332f87204c02204bbb86d6f85b151ef6ccc69bdec8061f6aefa6fdbe5e0fba552f777d0b7a3047014c69522103519f82ee14299c48513e5da8a64c06843006746c9af97fd38e2b9efe8c38651c21037b9d763cfc6464a9acd195a3768ef229722379f17d9a42e3c7ce5cc84a659c4d2102747921c1499ae5ed8cfed780fba7150e91948dd8c24b0e1fce332d344ec7a25d53aeffffffff639aa526788d4ce89b35dc96f86acfcb0b477048db4e81bb3dcd86f1b48e2f5405000000fdfe000048304502210095545373bd2d53139d7aa7248bbee6c5eacb9d992b7ed7d9e9eaa86d3262f7c702200c51fd08fba85ea263437f1737097c0e194ac0468040811d5d3c4c5e2ef5e40401483045022100b5c5138aaec6ea17c4a5e5d7699f347b44540ee2f7118c977c109370088ed39f022049a911f90361da56b9a00c10be77b31c440a15f3d111f01f23a207e2771baad2014c69522103c4c41066a17e20d769789f010da5d8500294d154de9332ef2b057f4ba82270b5210219d5c55069ff9ae0ebcd34852d50d2f7a67f3563e0d6256cdd59af9f02fec41b2103fe4e0377163b9660eda8a7ad480f7737d106cbad5b19460d1ee7689bce89242753aeffffffff16752f2504000000001976a9145a6f044a79b2cd0b3b55b550fdfca204a7cf3ef088ac00c40900000000001976a91445a252a1f64f26398ad3b445aa249ddf32500f2688ac4b623d00000000001976a9141d369b93b4039cb370b5d67c01293d7f3283692088ac68cb5f00000000001976a914086904da6330b6daeab823722e8e60cf9cae199f88ac00c40900000000001976a914206fbe7993f73c0ce4bda7cb8eb8f8632d991db888ac917d2600000000001976a914f72a8d95f3641abda939ddcd3df3452ee09eb54988ac70f30500000000001976a9148b803792ed1d4a8030235a710cee65ea201218a388acb0bd2800000000001976a914706db2b1b48af933fc7c76ee2aa7825faf2f818188aca0f70300000000001976a914dd9dcf2e6ca80585115ddb23ca41c6fe666d050388ac8753ad040000000017a914782c9f0200f1762964bee434628564a0263b78348700350c00000000001976a9148dff6490d5b62a25344d8225df86d9da13d0677888ac00c40900000000001976a914741780943ae861c21e2813494b77c7595fd460b688ac8811a700000000001976a9149595da2965db9e78d4d1bbc779b8d5b506ac6feb88ac65605401000000001976a91498187a90349aa74a73d636e8bff6a90600b1199288ac00c40900000000001976a91467baf9e8560960bd818751124ae45ff02f49b52e88acd0fb0100000000001976a914b5e845c2dcb4b3182980808412066be0bc7ace9c88ac40874200000000001976a9141aaa484ea70b405e4b76e7135e341a4150e2d6c788ace02963010000000017a91487a631a2b973504ed541ee019a372a265ef6ac4e8700c40900000000001976a914a547826a00b7a16f0a4e63128e7e9922ec6ac28d88ac07c52500000000001976a9144f11a6e85e92c224615bb7c4d9cc4e592ab6bb1c88ac00c40900000000001976a914fc1d2b0bd628c42a9d4c653efd523c03015a7cbc88acc06813000000000017a91420f5900279381f7d4bc29e5de1a46cbe6024b2518700000000";
		
		Transaction tx = new Transaction(params, Utils.HEX.decode(txString));
		
		tx.verify();
		
		System.out.println(tx.toString());

		assert Arrays.equals(
				tx.bitcoinSerialize(),
				Utils.HEX.decode(txString));
		
		assert tx.getParams().equals(params);
	}
	
	@SuppressWarnings("unused")
	private static void keyTest() {
		NetworkParameters params = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
		
		assert Arrays.equals(
				Utils.parseAsHexOrBase58("cQgNVSk8a7gAgtoWmmMMVU1zYwarS8Qw7Et1S6HG4tkqyU6FmFUC"), 
				Utils.parseAsHexOrBase58("ef5c6dcc4abbbd6ffbf31e37ecbc68cebc82896ae7619fbcca5065267196d2e36501"));
		
		ECKey k1 = ECKey.fromPrivate(Utils.parseAsHexOrBase58("cQgNVSk8a7gAgtoWmmMMVU1zYwarS8Qw7Et1S6HG4tkqyU6FmFUC"));	
		ECKey k2 = ECKey.fromPrivate(Utils.parseAsHexOrBase58("ef5c6dcc4abbbd6ffbf31e37ecbc68cebc82896ae7619fbcca5065267196d2e36501"));
		
		assert k1.equals(k2);
		
		assert Arrays.equals(
				k1.getPubKey(),
				k2.getPubKey());
				

		System.out.println("k1 pvt HEX: "+k1.getPrivateKeyAsHex());
		System.out.println("k2 pvt HEX: "+k2.getPrivateKeyAsHex());
		
		System.out.println("k1 pvt WiF: "+k1.getPrivateKeyAsWiF(params));
		System.out.println("k2 pvt WiF: "+k2.getPrivateKeyAsWiF(params));

		ECKey k3 = 		DumpedPrivateKey.fromBase58(params, "KwFXbpDYCohXSLt7ufUypQQY4CpG9emcfohHWeW1AvVg5i3kXAKS").getKey().decompress();
		ECKey k3comp = 	DumpedPrivateKey.fromBase58(params, "KwFXbpDYCohXSLt7ufUypQQY4CpG9emcfohHWeW1AvVg5i3kXAKS").getKey();

		System.out.println(k3.isCompressed());
		System.out.println(k3comp.isCompressed());
		System.out.println(k3.equals(k3comp));
		
		System.out.println("k3  pvt HEX: "+k3.getPrivateKeyAsHex());
		System.out.println("k3c pvt HEX: "+k3comp.getPrivateKeyAsHex());
		
		System.out.println("k3  pvt WiF: "+k3.getPrivateKeyAsWiF(params));
		System.out.println("k3c pvt WiF: "+k3comp.getPrivateKeyAsWiF(params));
		
		System.out.println("k3  pvt WiF HEX: "+Utils.HEX.encode(Base58.decodeChecked(k3.getPrivateKeyAsWiF(params))));
		System.out.println("k3c pvt WiF HEX: "+Utils.HEX.encode(Base58.decodeChecked(k3comp.getPrivateKeyAsWiF(params))));
		System.out.println("k3  pvt WiF HEX: "+Utils.HEX.encode(Base58.decode(k3.getPrivateKeyAsWiF(params))));
		System.out.println("k3c pvt WiF HEX: "+Utils.HEX.encode(Base58.decode(k3comp.getPrivateKeyAsWiF(params))));
		
		ECKey k4 = DumpedPrivateKey.fromBase58(params, Base58.encode(Utils.HEX.decode("8000ef5c6dcc4abbbd6ffbf31e37ecbc68cebc82896ae7619fbcca5065267196d2f141dadb"))).getKey();

		assert k4.equals(k3);
		
		System.out.println("k3  pub HEX: "+k3.getPublicKeyAsHex());
		System.out.println("k3c pub HEX: "+k3comp.getPublicKeyAsHex());
		
		System.out.println("k3  pub WiF: "+new Address(params, k3.getPubKeyHash()).toBase58());
		System.out.println("k3c pub WiF: "+new Address(params, k3comp.getPubKeyHash()).toBase58());
		
		System.out.println("k3  pub WiF HEX: "+Utils.HEX.encode(Base58.decodeChecked(new Address(params, k3.getPubKeyHash()).toBase58())));
		System.out.println("k3c pub WiF HEX: "+Utils.HEX.encode(Base58.decodeChecked(new Address(params, k3comp.getPubKeyHash()).toBase58())));
		System.out.println("k3  pub WiF HEX: "+Utils.HEX.encode(Base58.decode(new Address(params, k3.getPubKeyHash()).toBase58())));
		System.out.println("k3c pub WiF HEX: "+Utils.HEX.encode(Base58.decode(new Address(params, k3comp.getPubKeyHash()).toBase58())));
	}
	
	@SuppressWarnings("unused")
	private static void issueWiFRepresentation() {
		NetworkParameters params = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);

		BigInteger good = new BigInteger("5c6dcc4abbbd6ff562346234623464326bf31e37ecbc68cebc82896ae7619fbc", 16);	// exactly 32-bytes
		BigInteger bad 	= new BigInteger("5c6dcc4abbbd6ff562346234623464326bf31e37ecbc68cebc82896ae7619fbc88", 16); // exactly 33-bytes
		BigInteger bad2 = new BigInteger("5c6dcc4abbbd6ff562346234623464326bf31e37ecbc68cebc82896ae7619fbc88888888", 16); // > 33-bytes
		
		ECKey kGood = ECKey.fromPrivate(good);
		ECKey kBad =  ECKey.fromPrivate(bad);
		ECKey kBad2 = ECKey.fromPrivate(bad2);
		
		assert kGood.getPrivKey().equals(good);
		assert kBad.getPrivKey().equals(bad);
		assert kBad2.getPrivKey().equals(bad2);
		
		String kGoodWiF = kGood.getPrivateKeyAsWiF(params);		// KzKP2XkH93ytD9SdP7yKBwZERMHhwWTT5TXreWAwBhC1zMjQcBAE
		String kBadWiF = kBad.getPrivateKeyAsWiF(params);		// Kzu9JFTWLNChGScJqdzRCxg8SYopsiKkFwDw9GXLVm8LzajSoyhn
		String kBad2WiF = kBad2.getPrivateKeyAsWiF(params);		// KzKP2XkH93ytD9SdP7yKBwZERMHhwWTT5TXreWAwBhC1zMjQcBAE

		System.out.println(kGoodWiF);
		System.out.println(kBadWiF);
		System.out.println(kBad2WiF);
		
		ECKey k5GoodCopy = DumpedPrivateKey.fromBase58(params, kGoodWiF).getKey();
		ECKey k5BadCopy =  DumpedPrivateKey.fromBase58(params, kBadWiF).getKey();
		ECKey k5Bad2Copy =  DumpedPrivateKey.fromBase58(params, kBad2WiF).getKey();
		
		System.out.println(k5GoodCopy.getPrivKey().toString(16));
		System.out.println(k5BadCopy.getPrivKey().toString(16));
		System.out.println(k5Bad2Copy.getPrivKey().toString(16));
		
		assert k5GoodCopy.getPrivKey().equals(good);	// 
//		assert k5BadCopy.getPrivKey().equals(bad);		// k5BadCopy.getPrivKey()  == 6dcc4abbbd6ff562346234623464326bf31e37ecbc68cebc82896ae7619fbc88
//		assert k5Bad2Copy.getPrivKey().equals(bad2);	// k5Bad2Copy.getPrivKey() == 5c6dcc4abbbd6ff562346234623464326bf31e37ecbc68cebc82896ae7619fbc
	}

}
