/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.web

import com.google.gson.Gson
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.eclipse.xtend.lib.annotations.Accessors

@WebServlet(name = 'WebUtils', urlPatterns = '/new-key')
class WebUtilsServlet extends HttpServlet {

    val gson = new Gson

    @Accessors
    private static class Key {
        val String privkeyTestnet
        val String privkeyMainnet
        val String pubkey
        val String addressTestnet
        val String addressMainnet
    }

    override protected doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        val k = new ECKey
        val privkeyTestnet = k.getPrivateKeyEncoded(TestNet3Params.get).toBase58
        val privkeyMainnet = k.getPrivateKeyEncoded(MainNetParams.get).toBase58
        val publickey = k.publicKeyAsHex
        val addressTestnet = LegacyAddress.fromPubKeyHash(TestNet3Params.get, k.pubKeyHash).toBase58
        val addressMainnet = LegacyAddress.fromPubKeyHash(MainNetParams.get, k.pubKeyHash).toBase58
        val result = new Key(privkeyTestnet, privkeyMainnet, publickey, addressTestnet, addressMainnet)
        response.contentType = 'application/json'
        response.status = HttpServletResponse.SC_OK
        gson.toJson(result, response.writer)
    }
}