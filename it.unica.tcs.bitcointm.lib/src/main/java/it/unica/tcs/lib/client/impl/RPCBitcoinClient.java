/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.lib.client.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sulacosoft.bitcoindconnector4j.BitcoindApi;
import com.sulacosoft.bitcoindconnector4j.BitcoindApiFactory;
import com.sulacosoft.bitcoindconnector4j.core.BitcoindException;
import com.sulacosoft.bitcoindconnector4j.core.RPCErrorCode;
import com.sulacosoft.bitcoindconnector4j.response.RawTransaction;

import it.unica.tcs.lib.client.BitcoinClientException;
import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.Confidentiality;
import it.unica.tcs.lib.client.TransactionNotFoundException;

public class RPCBitcoinClient implements BitcoinClientI {

    private static final Logger logger = LoggerFactory.getLogger(RPCBitcoinClient.class);
    private BitcoindApi api;

    private final String address;
    private final int port;
    private final String protocol;
    private final String user;
    private final String password;
    private final int timeout;
    private final TimeUnit unit;

    public RPCBitcoinClient(String address, int port, String protocol, String user, String password, int timeout, TimeUnit unit) {
        this.address = address;
        this.port = port;
        this.protocol = protocol;
        this.user = user;
        this.password = password;
        this.timeout = timeout;
        this.unit = unit;
    }

    public BitcoindApi getApi() throws BitcoinClientException {
        if (this.api==null) {
            try {
                this.api = BitcoindApiFactory.createConnection(address, port, protocol, "/", user, password, timeout, unit);
            }
            catch (Throwable e) {
                logger.warn("Unable to create a BitcoinApi object. Error: '{}'", e.getMessage());
                throw new BitcoinClientException(e);
            }
        }
        return api;
    }

    @Override
    public int getBlockCount() {
        try {
            return getApi().getblockcount();
        }
        catch (Throwable e) {
            throw new BitcoinClientException(e);
        }
    }

    @Override
    public String getRawTransaction(String txid) throws TransactionNotFoundException {
        if (!isMined(txid, Confidentiality.LOW))
            throw new TransactionNotFoundException();
        try {
            return getApi().getrawtransaction(txid);
        }
        catch (Throwable e) {
            throw new BitcoinClientException(e);
        }
    }

    @Override
    public boolean isMined(String txid) {
        return isMined(txid, Confidentiality.HIGH);
    }

    @Override
    public boolean isMined(String txid, Confidentiality reliability) {
        try {
            RawTransaction tx = getApi().getrawtransaction(txid, true);
            return tx.getConfirmations() >= reliability.getConfirmations();
        }
        catch (BitcoindException e) {
            if (e.getCode() == RPCErrorCode.RPC_INVALID_ADDRESS_OR_KEY)
                return false;
            throw new BitcoinClientException(e);
        }
        catch (Throwable e) {
            throw new BitcoinClientException(e);
        }
    }

    @Override
    public String sendRawTransaction(String transaction) {
        try {
            return getApi().sendrawtransaction(transaction);
        }
        catch (Throwable e) {
            throw new BitcoinClientException(e);
        }
    }

    @Override
    public boolean isUTXO(String txid) throws TransactionNotFoundException {
        return isUTXO(txid, 0);
    }

    @Override
    public boolean isUTXO(String txid, int n) throws TransactionNotFoundException {
        if (!isMined(txid, Confidentiality.LOW))
            throw new TransactionNotFoundException();
        try {
            return this.getApi().gettxout(txid, n)!=null;
        }
        catch (Throwable e) {
            throw new BitcoinClientException(e);
        }
    }
}
