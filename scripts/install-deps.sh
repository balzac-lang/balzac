[[ "${BITCOINJ_TAG}" == "" ]] && (echo "BITCOINJ_TAG is not set" && exit 1)

rm -rf $HOME/bitcoinj || (echo "Unable to delete dir $HOME/bitcoinj" && exit 1)

echo "Cloning https://github.com/natzei/bitcoinj.git"
git -C $HOME clone https://github.com/natzei/bitcoinj.git || (echo "Unable to clone https://github.com/natzei/bitcoinj.git" && exit 1)
git -C $HOME/bitcoinj checkout ${BITCOINJ_TAG} || (echo "Unable to checkout branch ${BITCOINJ_TAG}" && exit 1)
$HOME/bitcoinj/gradlew -p $HOME/bitcoinj publishToMavenLocal -x test || (echo "Unable to gradle install bitcoinj" && exit 1)
rm -rf $HOME/bitcoinj || (echo "Unable to delete dir $HOME/bitcoinj" && exit 1)
