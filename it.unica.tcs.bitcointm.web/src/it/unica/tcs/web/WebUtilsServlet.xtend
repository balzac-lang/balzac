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
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.lib.Hash.HashAlgorithm

@WebServlet(name = 'WebUtils', urlPatterns = '/utils/*')
class WebUtilsServlet extends HttpServlet {

    val gson = new Gson

    @Accessors
    private static class KeyResult {
        val String privkeyTestnet
        val String privkeyMainnet
        val String pubkey
        val String addressTestnet
        val String addressMainnet
    }

    @Accessors
    private static class HashResult {
        val String value
        val String type
        val String sha1
        val String sha256
        val String ripemd160
        val String hash256
        val String hash160
    }

    override protected doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        if (req.requestURI.contains("/utils/new-key")) {
            val k = new ECKey
            val privkeyTestnet = k.getPrivateKeyEncoded(TestNet3Params.get).toBase58
            val privkeyMainnet = k.getPrivateKeyEncoded(MainNetParams.get).toBase58
            val publickey = k.publicKeyAsHex
            val addressTestnet = LegacyAddress.fromPubKeyHash(TestNet3Params.get, k.pubKeyHash).toBase58
            val addressMainnet = LegacyAddress.fromPubKeyHash(MainNetParams.get, k.pubKeyHash).toBase58
            val result = new KeyResult(privkeyTestnet, privkeyMainnet, publickey, addressTestnet, addressMainnet)
            response.contentType = 'application/json'
            response.status = HttpServletResponse.SC_OK
            gson.toJson(result, response.writer)
            return
        }
        else if (req.requestURI.contains("/utils/hash")) {
            response.contentType = 'application/json'
            response.status = HttpServletResponse.SC_OK

            val value = req.getParameter("of")

            // Parameter check
            if (value === null) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                return
            }

            try {
                /*
                 * Try to interpret value as Integer
                 */
                gson.toJson(hash(Integer.parseInt(value)), response.writer)
                return
            }
            catch (NumberFormatException e) {
                /*
                 * Try to interpret true/false
                 */
                if (value.toLowerCase == "true") {
                    gson.toJson(hash(true), response.writer)
                    return
                }
                
                if (value.toLowerCase == "false") {
                    gson.toJson(hash(false), response.writer)
                    return
                }

                /*
                 * Finally treat them as String
                 */
                val strValue =
                    if (!value.isEmpty && value.charAt(0) == Character.valueOf('"') && value.charAt(value.length - 1) == Character.valueOf('"'))
                    value.substring(1, value.length-1).replace("\\\"", "\"")
                    else value

                gson.toJson(hash(strValue), response.writer)
                return
            }
        }
        response.status = HttpServletResponse.SC_NOT_FOUND
    }
    
    private def HashResult hash(Object b) {
        new HashResult(
            b.toString,
            b.class.simpleName,
            BitcoinUtils.hash(b, HashAlgorithm.SHA1).toString,
            BitcoinUtils.hash(b, HashAlgorithm.SHA256).toString,
            BitcoinUtils.hash(b, HashAlgorithm.RIPEMD160).toString,
            BitcoinUtils.hash(b, HashAlgorithm.HASH256).toString,
            BitcoinUtils.hash(b, HashAlgorithm.HASH160).toString
        )
    }
}