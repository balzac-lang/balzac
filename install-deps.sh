echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git
gradle -p $HOME/BitcoindConnector4J install
rm -rf $HOME/BitcoindConnector4J

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git
git -C $HOME/bitcoinj checkout lib
mvn -f $HOME/bitcoinj install -DskipTests
rm -rf $HOME/bitcoinj
