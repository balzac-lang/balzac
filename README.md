[![Build Status](https://travis-ci.org/balzac-lang/balzac.svg?branch=master)](https://travis-ci.org/balzac-lang/balzac)
[![Coverage Status](https://coveralls.io/repos/github/balzac-lang/balzac/badge.svg)](https://coveralls.io/github/balzac-lang/balzac)

# Balzac

A domain-specific language to write Bitcoin transactions, based on the paper
[A formal model of Bitcoin transactions](https://eprint.iacr.org/2017/1124.pdf) presented at [Financial Cryptography and Data Security 2018](http://fc18.ifca.ai/).

**Online editor**:

- [http://blockchain.unica.it/balzac/](http://blockchain.unica.it/balzac/)
- [http://balzac-lang.xyz](http://balzac-lang.xyz) (mirror)

**Documentation**

- online: [https://blockchain.unica.it/balzac/docs](https://blockchain.unica.it/balzac/docs)
- repository: [https://github.com/balzac-lang/balzac-doc](https://github.com/balzac-lang/balzac-doc)




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

## Install
```
mvn -f xyz.balzaclang.balzac.parent/ -U clean install
```

## Run Balzac

In order to run Balzac locally you can

- build on your machine and run using Maven + Jetty
- build on your machine and load the war (saved into `xyz.balzaclang.balzac.web/target/`) inside any web container (e.g. Tomcat)
- build on your machine and run in Docker (see `docker/Dockerfile-slim` to create the image)
- build and run in Docker (see `docker/Dockerfile-build` to create the image)
- download from DockerHub and run in Docker

### Maven + Jetty

```
mvn -f xyz.balzaclang.balzac.web/ jetty:run
```

### Build with Maven, run with Docker

```
docker build -f docker/Dockerfile-slim -t balzac:latest .
docker run -p 8080:8080 balzac:latest
```

### Build and run with Docker

```
docker build -f docker/Dockerfile-build -t balzac:latest .
docker run -p 8080:8080 balzac:latest
```

### Prebuild image

DockerHub: [balzaclang/balzac](https://hub.docker.com/r/balzaclang/balzac)

```
docker pull balzaclang/balzac:latest
docker run -p 8080:8080 balzaclang/balzac:latest
```


## Development

The project is currently developed using *Eclipse IDE for Java and DSL Developers* (generally the latest version).
Install it using the [Eclipse installer](http://www.eclipse.org/downloads/eclipse-packages/).

Dependencies
- Xsemantics 1.17 ([update site](http://download.eclipse.org/xsemantics/milestones/1.17/))
- Jacoco (optional) (see [http://www.eclemma.org/installation.html](http://www.eclemma.org/installation.html))

