rm -rf $HOME/BitcoindConnector4J
rm -rf $HOME/bitcoinj

echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git
git -C $HOME/BitcoindConnector4J checkout v0.16.0
./gradlew -p $HOME/BitcoindConnector4J install
rm -rf $HOME/BitcoindConnector4J

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git
git -C $HOME/bitcoinj checkout 0.16.1-LIB
./gradlew -p $HOME/bitcoinj install -x test
rm -rf $HOME/bitcoinj
