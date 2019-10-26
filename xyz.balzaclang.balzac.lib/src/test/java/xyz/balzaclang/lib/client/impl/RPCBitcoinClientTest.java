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
package xyz.balzaclang.lib.client.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.sulacosoft.bitcoindconnector4j.BitcoindApi;

import xyz.balzaclang.lib.client.Confidentiality;
import xyz.balzaclang.lib.client.TransactionNotFoundException;

@Ignore
public class RPCBitcoinClientTest {

    RPCBitcoinClient client = new RPCBitcoinClient("co2.unica.it", 18332, "http", "/", "bitcoin", "L4mbWnzC35BNrmTJ", 3, TimeUnit.SECONDS);

    @Test
    public void test_getRawTransaction() {
        String expected = "01000000018a636a44915cb0e7bbd038819cd1455cd974e007778de40f918d988c5d216831000000008e483045022100bd0370e1868ab7bfbdbc58442212450f3bb06eac5daced1995f610e50ce29cda02202b9d2b384097d63791cb64f03009e937095393414f368c45e9ab178a26406fce0104626172743f6b6b6c21032b6cb7aa033a063d2dc39573bde12a2d01e20a971d6d4f85eb27ad0793b3689cac6ca91443dacdc2e924bf0429e3b5f904659ed9fd00e71e879affffffff02e0e29a0b000000001976a91472832b2c182d98fc38b98bbdc76318ee20ac9c3588ac00000000000000000b6a09697420776f726b732100000000";

        try {
            String hex = client.getRawTransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845");
            assertEquals(expected, hex);
        } catch (TransactionNotFoundException e) {
            fail();
        }
    }

    @Test(expected=TransactionNotFoundException.class)
    public void test_getRawTransaction_notFound() {
        client.getRawTransaction("0000000000000000000000000000000000000000000000000000000000000000");
    }

    @Test
    public void test_isMined() {
        assertTrue(client.isMined("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        assertFalse(client.isMined("0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    public void test_isUTXO() {
        assertFalse(client.isUTXO("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
    }

    @Test(expected=TransactionNotFoundException.class)
    public void test_isUTXO_notFound() {
        client.isUTXO("0000000000000000000000000000000000000000000000000000000000000000");
    }

    @Test
    public void test() {

        BitcoindApi api = ((RPCBitcoinClient)client).getApi();

        System.out.println("Best block count: " + client.getBlockCount());
        System.out.println("Get raw transaction: " + client.getRawTransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("ee40379cdf5439983d7603a88cafdd6de1c20d3b164850ab1055ed276ed95468"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", true));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", false));
        System.out.println("Get UTXO: " + api.gettxout("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 0));
        System.out.println("Get UTXO: " + api.gettxout("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 2));
        System.out.println("Get isUTXO: " + client.isUTXO("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get isUTXO: " + client.isUTXO("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 1));
        System.out.println("Is mined: " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66"));
        System.out.println("Is mined (reliability low): " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66", Confidentiality.LOW));
    }

    public static void main(String[] args) {
        RPCBitcoinClient client = new RPCBitcoinClient("co2.unica.it", 80, "http", "/bitcoin-testnet", "bitcoin", "L4mbWnzC35BNrmTJ", 3, TimeUnit.SECONDS);
        BitcoindApi api = ((RPCBitcoinClient)client).getApi();

        System.out.println("Best block count: " + client.getBlockCount());
        System.out.println("Get raw transaction: " + client.getRawTransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("ee40379cdf5439983d7603a88cafdd6de1c20d3b164850ab1055ed276ed95468"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", true));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", false));
        System.out.println("Get UTXO: " + api.gettxout("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 0));
        System.out.println("Get UTXO: " + api.gettxout("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 2));
        System.out.println("Get isUTXO: " + client.isUTXO("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get isUTXO: " + client.isUTXO("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 1));
        System.out.println("Is mined: " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66"));
        System.out.println("Is mined (reliability low): " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66", Confidentiality.LOW));
    }
}
