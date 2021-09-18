[![Build Status](https://app.travis-ci.com/balzac-lang/balzac.svg?branch=master)](https://app.travis-ci.com/balzac-lang/balzac)
[![Coverage Status](https://coveralls.io/repos/github/balzac-lang/balzac/badge.svg)](https://coveralls.io/github/balzac-lang/balzac)
[![Donate with Bitcoin](https://en.cryptobadges.io/badge/micro/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)](https://en.cryptobadges.io/donate/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)

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
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"
mvn -f xyz.balzaclang.balzac.parent/ -U clean install
```

## Run Balzac

In order to run Balzac locally you can

- build on your machine and run using Maven + Jetty
- build on your machine and load the war (saved into `xyz.balzaclang.balzac.web/target/`) inside any web container (e.g. Tomcat)
- build on your machine and run in Docker (see `docker/Dockerfile-slim` to create the image)
- download from DockerHub and run in Docker

### Maven + Jetty

```
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"
mvn -f xyz.balzaclang.balzac.web/ jetty:run
```

### Build with Maven, run with Docker

```
docker build -f docker/Dockerfile-slim -t balzac:latest .
docker run -p 8080:8080 balzac:latest
```

### Prebuilt image

DockerHub: [balzaclang/balzac](https://hub.docker.com/r/balzaclang/balzac)

```
docker pull balzaclang/balzac:latest
docker run -p 8080:8080 balzaclang/balzac:latest
```


## Development

The project is currently developed using *Eclipse IDE for Java and DSL Developers* (generally the latest version).
Install it using the [Eclipse installer](http://www.eclipse.org/downloads/eclipse-packages/).

Dependencies
- [Xsemantics](https://github.com/eclipse/xsemantics)
- [Jacoco](http://www.eclemma.org/installation.html) (optional)

## Donate

[![Donate with Bitcoin](https://en.cryptobadges.io/badge/big/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)](https://en.cryptobadges.io/donate/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)
