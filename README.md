[![Build Status](https://github.com/balzac-lang/balzac/actions/workflows/maven.yml/badge.svg)](https://github.com/balzac-lang/balzac/actions/workflows/maven.yml)
[![Coverage Status](https://coveralls.io/repos/github/balzac-lang/balzac/badge.svg)](https://coveralls.io/github/balzac-lang/balzac)
[![Donate with Bitcoin](https://en.cryptobadges.io/badge/micro/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)](https://en.cryptobadges.io/donate/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)

# Balzac

A domain-specific language to write Bitcoin transactions, based on the paper
[A formal model of Bitcoin transactions](https://eprint.iacr.org/2017/1124.pdf) presented at [Financial Cryptography and Data Security 2018](http://fc18.ifca.ai/).

**Online editor**: [balzac-lang.xyz](http://balzac-lang.xyz)

**Documentation**: [docs.balzac-lang.xyz](http://docs.balzac-lang.xyz)

## Quickstart

```
docker pull balzaclang/balzac:latest
docker run --rm -p 8080:8080 --name balzac balzaclang/balzac:latest
```

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


The following commands assume this variable has been set:

```
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"
```


### Build a WAR package

```
mvn -f xyz.balzaclang.balzac.web/ package
```

The WAR package is saved in `xyz.balzaclang.balzac.web/target/`.


### Run with Maven + Jetty

```
mvn -f xyz.balzaclang.balzac.web/ jetty:run
```

Or alternatively

```
mvn -f xyz.balzaclang.balzac.web/ jetty:run-war
```

### Build Docker image

Assuming the WAR package is saved in `xyz.balzaclang.balzac.web/target/`, you can generate the Docker image by executing:

```
docker build -t balzaclang/balzac:latest .
```

You can specify a custom war file by using the `build-arg war=<path>` parameter.


### Prebuilt image

DockerHub: [balzaclang/balzac](https://hub.docker.com/r/balzaclang/balzac)

```
docker pull balzaclang/balzac:latest
```

Balzac is available for the following architectures:

```
- linux/amd64
- linux/arm/v7
- linux/arm64
```


### Docker run

```
docker run --rm -p 8080:8080 --name balzac balzaclang/balzac:latest
```


## Development

The project is currently developed using *Eclipse IDE for Java and DSL Developers* (generally the latest version).
Install it using the [Eclipse installer](http://www.eclipse.org/downloads/eclipse-packages/).

Dependencies
- [Xsemantics](https://github.com/eclipse/xsemantics)
- [Jacoco](http://www.eclemma.org/installation.html) (optional)

## Donate

[![Donate with Bitcoin](https://en.cryptobadges.io/badge/big/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)](https://en.cryptobadges.io/donate/1BALZaCuLUEnGYv6TJLMkya6QG1oxM8fKg)
