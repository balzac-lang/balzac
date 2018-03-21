[![Build Status](https://travis-ci.org/bitcoin-transaction-model/bitcoin-transaction-model.svg?branch=master)](https://travis-ci.org/bitcoin-transaction-model/bitcoin-transaction-model)
[![Coverage Status](https://coveralls.io/repos/github/bitcoin-transaction-model/bitcoin-transaction-model/badge.svg?branch=master)](https://coveralls.io/github/bitcoin-transaction-model/bitcoin-transaction-model?branch=master)

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
git checkout release-0.16
gradle install
cd ..
```

[Compare versions](https://github.com/SulacoSoft/BitcoindConnector4J/compare/master...natzei:master)



### Compile
```
mvn -f bitcoin-transaction-model/it.unica.tcs.bitcointm.parent/ clean install
```

### Run standalone server

In order to locally run the server (same of [http://blockchain.unica.it/btm/](http://blockchain.unica.it/btm/)):

```
mvn -f it.unica.tcs.bitcointm.web/ jetty:run
```



### IDE

The project is currently developed using [Eclipse Xtext](https://www.eclipse.org/Xtext/download.html), version 2.12.

Install Xsemantics 2.12 ([update site](https://dl.bintray.com/lorenzobettini/xsemantics/updates/)).

Optional: install Jacoco (see [http://www.eclemma.org/installation.html](http://www.eclemma.org/installation.html)).

