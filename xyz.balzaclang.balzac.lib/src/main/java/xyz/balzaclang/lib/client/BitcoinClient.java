/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.balzaclang.lib.client;

public interface BitcoinClient {

    /**
     * Return the number of blocks of the blockchain.
     * @return the number of blocks of the blockchain.
     */
    public int getBlockCount();

    /**
     * Retrieve the hex payload from the given transaction id.
     * @param txid the id of the transaction.
     * @return a string encoding of the payload of the transaction.
     * @throws TransactionNotFoundException if the transaction is not within the blockchain.
     */
    public String getRawTransaction(String txid) throws TransactionNotFoundException;

    /**
     * Check if the transaction with the specified txid is mined.
     * @param txid the id of the transaction.
     * @return true if the transaction is mined, false otherwise.
     */
    public boolean isMined(String txid);

    /**
     * Check if the transaction with the specified txid is mined.
     * @param txid the id of the transaction.
     * @param confidentiality the confidentiality of the check.
     * @return true if the transaction is mined, false otherwise.
     */
    public boolean isMined(String txid, Confidentiality confidentiality);

    /**
     * Send the given transaction (hex encoded as string) to the network.
     * @param transaction transaction hex encoded as string
     * @return the id of the transaction.
     */
    public String sendRawTransaction(String transaction);

    /**
     * Check if the first output of the given transaction is unspent.
     * @param txid the id of the transaction.
     * @return true if the first output is unspent, false otherwise.
     * @throws TransactionNotFoundException if the transaction is not within the blockchain.
     */
    public boolean isUTXO(String txid) throws TransactionNotFoundException;

    /**
     * Check if the n-th output of the given transaction is unspent.
     * @param txid the id of the transaction.
     * @param n the n-th output to check
     * @return true if the n-th output is unspent, false otherwise.
     * @throws TransactionNotFoundException if the transaction is not within the blockchain.
     */
    public boolean isUTXO(String txid, int n) throws TransactionNotFoundException;

    /**
     * Check if the client is connected to the Bitcoin testnet.
     * @return true if the client is connected to the Bitcoin testnet.
     */
    public boolean isTestnet();

    /**
     * Check if the client is connected to the Bitcoin mainnet.
     * @return true if the client is connected to the Bitcoin mainnet.
     */
    public boolean isMainnet();

}
