echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git
gradle -p $HOME/BitcoindConnector4J install
rm -rf $HOME/BitcoindConnector4J

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git
git -C $HOME/bitcoinj checkout lib
mvn -f $HOME/bitcoinj install -DskipTests
rm -rf $HOME/bitcoinj

echo "Symlink it.unica.tcs.bitcointm/it.unica.tcs.bitcointm.lib-0.0.1-bundled.jar -> ../it.unica.tcs.bitcointm.lib/target/it.unica.tcs.bitcointm.lib-0.0.1-bundled.jar"
ln -t it.unica.tcs.bitcointm -s ../it.unica.tcs.bitcointm.lib/target/it.unica.tcs.bitcointm.lib-0.0.1-bundled.jar

