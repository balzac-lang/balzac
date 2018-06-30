[![Build Status](https://travis-ci.org/balzac-lang/balzac.svg?branch=master)](https://travis-ci.org/balzac-lang/balzac)
[![Coverage Status](https://coveralls.io/repos/github/balzac-lang/balzac/badge.svg)](https://coveralls.io/github/balzac-lang/balzac)

# Balzac

A domain-specific language to write Bitcoin transactions, based on the paper
[A formal model of Bitcoin transactions](https://eprint.iacr.org/2017/1124.pdf) presented at [Financial Cryptography and Data Security 2018](http://fc18.ifca.ai/).


## Online editor

Try the online editor [http://blockchain.unica.it/balzac/](http://blockchain.unica.it/balzac/).

## Development Setup

- clone the repository
- install some maven dependency
- symbolic links
- compile

### Clone the repository
```
git clone https://github.com/balzac-lang/balzac.git
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
mvn -f it.unica.tcs.bitcointm.parent/ clean install
```

### Run standalone server

In order to locally run the server (same of [http://blockchain.unica.it/btm/](http://blockchain.unica.it/btm/)):

```
mvn -f it.unica.tcs.bitcointm.web/ jetty:run
```



### IDE

The project is currently developed using *Eclipse IDE for Java and DSL Developers* (Oxygen).
Install it using the [Eclipse installer](http://www.eclipse.org/downloads/eclipse-packages/).

Install Xtext 2.14 ([update site](http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/)).

Install Xsemantics 1.14 ([update site](http://download.eclipse.org/xsemantics/milestones/1.14/)).

Optional: install Jacoco (see [http://www.eclemma.org/installation.html](http://www.eclemma.org/installation.html)).

