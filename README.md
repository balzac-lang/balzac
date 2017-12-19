# Bitcoin transaction model

A domain-specific language to write Bitcoin transactions, based on the paper
[A formal model of Bitcoin transactions](https://eprint.iacr.org/2017/1124.pdf) presented at [Financial Cryptography and Data Security 2018](http://fc18.ifca.ai/).


## Online editor

Try the online editor [http://blockchain.unica.it/btm/](http://blockchain.unica.it/btm/).

## Development Setup

- clone the repository
- install some maven dependency
- symbolic links
- compile

### Clone the repository
```
git clone https://github.com/bitcoin-transaction-model/bitcoin-transaction-model.git
```

### Install dependencies

#### bitcoinj

Install a customized version of bitcoinj (segwit branch) into your local maven repository:
```
git clone https://github.com/natzei/bitcoinj.git
cd bitcoinj
git checkout lib
mvn install -DskipTests
cd ..
```

[Compare versions](https://github.com/bitcoinj/bitcoinj/compare/segwit...natzei:lib)

#### BitcoindConnector4J

```
git clone https://github.com/natzei/BitcoindConnector4J.git
cd BitcoindConnector4J
gradle install
cd ..
```

[Compare versions](https://github.com/SulacoSoft/BitcoindConnector4J/compare/master...natzei:master)


### Simbolic Links

```
cd bitcoin-transaction-model/it.unica.tcs.bitcointm
ln -s ../it.unica.tcs.bitcointm.lib/target/it.unica.tcs.bitcointm.lib-0.0.1-bundled.jar
```

```
cd bitcoin-transaction-model/it.unica.tcs.bitcointm.ui
ln -s ../it.unica.tcs.bitcointm.lib/target/it.unica.tcs.bitcointm.lib-0.0.1-bundled.jar
```


### Compile
```
cd bitcoin-transaction-model/it.unica.tcs.bitcointm.parent/
mvn install
```

### IDE

The project is currently developed using [Eclipse Xtext](https://www.eclipse.org/Xtext/download.html).


