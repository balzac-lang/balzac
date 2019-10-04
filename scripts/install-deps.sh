[[ "${BITCOIND_CONNECTOR_TAG}" == "" ]] && (echo "BITCOIND_CONNECTOR_TAG is not set" && exit 1)
[[ "${BITCOINJ_TAG}" == "" ]] && (echo "BITCOINJ_TAG is not set" && exit 1)

rm -rf $HOME/BitcoindConnector4J || (echo "Unable to delete dir $HOME/BitcoindConnector4J" && exit 1)
rm -rf $HOME/bitcoinj || (echo "Unable to delete dir $HOME/bitcoinj" && exit 1)

echo "Cloning https://github.com/natzei/BitcoindConnector4J.git"
git -C $HOME clone https://github.com/natzei/BitcoindConnector4J.git || (echo "Unable to clone https://github.com/natzei/BitcoindConnector4J.git" && exit 1)
git -C $HOME/BitcoindConnector4J checkout "${BITCOIND_CONNECTOR_TAG}" || (echo "Unable to checkout branch ${BITCOIND_CONNECTOR_TAG}" && exit 1)
./gradlew -p $HOME/BitcoindConnector4J install || (echo "Unable to gradle install $HOME/BitcoindConnector4J" && exit 1)
rm -rf $HOME/BitcoindConnector4J || (echo "Unable to delete dir $HOME/BitcoindConnector4J" && exit 1)

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git || (echo "Unable to clone https://github.com/natzei/bitcoinj.git" && exit 1)
git -C $HOME/bitcoinj checkout ${BITCOINJ_TAG} || (echo "Unable to checkout branch ${BITCOINJ_TAG}" && exit 1)
./gradlew -p $HOME/bitcoinj install -x test || (echo "Unable to gradle install bitcoinj" && exit 1)
rm -rf $HOME/bitcoinj || (echo "Unable to delete dir $HOME/bitcoinj" && exit 1)
