rm -rf $HOME/BitcoindConnector4J
rm -rf $HOME/bitcoinj

echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git
git -C $HOME/BitcoindConnector4J checkout v0.16.0
gradle -p $HOME/BitcoindConnector4J install
rm -rf $HOME/BitcoindConnector4J

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git
git -C $HOME/bitcoinj checkout lib
gradle -p $HOME/bitcoinj install -x test
rm -rf $HOME/bitcoinj
