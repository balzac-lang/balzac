rm -rf $HOME/BitcoindConnector4J || (echo "Unable to delete dir $HOME/BitcoindConnector4J" && exit 1)
rm -rf $HOME/bitcoinj || (echo "Unable to delete dir $HOME/bitcoinj" && exit 1)

echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git || (echo "Unable to clone https://github.com/natzei/BitcoindConnector4J.git" && exit 1)
git -C $HOME/BitcoindConnector4J checkout v0.16.0 || (echo "Unable to checkout branch v0.16.0" && exit 1)
./gradlew -p $HOME/BitcoindConnector4J install || (echo "Unable to gradle install $HOME/BitcoindConnector4J" && exit 1)
rm -rf $HOME/BitcoindConnector4J || (echo "Unable to delete dir $HOME/BitcoindConnector4J" && exit 1)

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git || (echo "Unable to clone https://github.com/natzei/bitcoinj.git" && exit 1)
git -C $HOME/bitcoinj checkout 0.16.2-LIB || (echo "Unable to checkout branch 0.16.2-LIB" && exit 1)
./gradlew -p $HOME/bitcoinj install -x test || (echo "Unable to gradle install bitcoinj" && exit 1)
rm -rf $HOME/bitcoinj || (echo "Unable to delete dir $HOME/bitcoinj" && exit 1)
