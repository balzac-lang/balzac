[![Build Status](https://travis-ci.org/balzac-lang/balzac.svg?branch=master)](https://travis-ci.org/balzac-lang/balzac)
[![Coverage Status](https://coveralls.io/repos/github/balzac-lang/balzac/badge.svg)](https://coveralls.io/github/balzac-lang/balzac)

# Balzac

A domain-specific language to write Bitcoin transactions, based on the paper
[A formal model of Bitcoin transactions](https://eprint.iacr.org/2017/1124.pdf) presented at [Financial Cryptography and Data Security 2018](http://fc18.ifca.ai/).

**Online editor**: [http://blockchain.unica.it/balzac/](http://blockchain.unica.it/balzac/)

**Documentation**: [http://blockchain.unica.it/balzac/docs](http://blockchain.unica.it/balzac/docs)

## Setup

Execute the script `install-deps.sh` or alternatively follow these steps:

**Install a customized version of BitcoindConnector4J**
```
echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git
git -C $HOME/BitcoindConnector4J checkout release-0.16
gradle -p $HOME/BitcoindConnector4J install
```
[Compare versions](https://github.com/SulacoSoft/BitcoindConnector4J/compare/master...natzei:master)

**Install a customized version of BitcoinJ**
```
echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git
git -C $HOME/bitcoinj checkout lib
gradle -p $HOME/bitcoinj install -x test
```
[Compare versions](https://github.com/bitcoinj/bitcoinj/compare/master...natzei:lib)

### Install
```
mvn -f it.unica.tcs.bitcointm.parent/ clean install
```

### Run standalone server

In order to locally run the server (same of [http://blockchain.unica.it/btm/](http://blockchain.unica.it/btm/)):

```
mvn -f it.unica.tcs.bitcointm.web/ jetty:run
```



### IDE

The project is currently developed using *Eclipse IDE for Java and DSL Developers* (Photon).
Install it using the [Eclipse installer](http://www.eclipse.org/downloads/eclipse-packages/).

Install Xtext 2.14 ([update site](http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/)).

Install Xsemantics 1.14 ([update site](http://download.eclipse.org/xsemantics/milestones/1.14/)).

Optional: install Jacoco (see [http://www.eclemma.org/installation.html](http://www.eclemma.org/installation.html)).

