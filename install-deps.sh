rm -rf $HOME/BitcoindConnector4J
rm -rf $HOME/bitcoinj

echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git
git -C $HOME/BitcoindConnector4J checkout release-0.16
gradle -p $HOME/BitcoindConnector4J install
rm -rf $HOME/BitcoindConnector4J

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git
git -C $HOME/bitcoinj checkout lib
mvn -f $HOME/bitcoinj install -DskipTests
rm -rf $HOME/bitcoinj
