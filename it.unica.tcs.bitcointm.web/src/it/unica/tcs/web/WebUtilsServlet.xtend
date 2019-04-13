/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.web

import com.google.gson.Gson
import it.unica.tcs.lib.model.Address
import it.unica.tcs.lib.model.Hash
import it.unica.tcs.lib.model.Hash.HashAlgorithm
import it.unica.tcs.lib.model.NetworkType
import it.unica.tcs.lib.model.PrivateKey
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.xtend.lib.annotations.Accessors

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
            val key = PrivateKey.fresh(NetworkType.TESTNET)
            val privkeyTestnet = key.wif
            val privkeyMainnet = PrivateKey.copy(key, NetworkType.MAINNET).wif
            val publickey = key.toPublicKey.bytesAsString
            val addressTestnet = Address.from(key.toPublicKey, NetworkType.TESTNET).wif
            val addressMainnet = Address.from(key.toPublicKey, NetworkType.MAINNET).wif
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
            Hash.hash(b, HashAlgorithm.SHA1).toString,
            Hash.hash(b, HashAlgorithm.SHA256).toString,
            Hash.hash(b, HashAlgorithm.RIPEMD160).toString,
            Hash.hash(b, HashAlgorithm.HASH256).toString,
            Hash.hash(b, HashAlgorithm.HASH160).toString
        )
    }
}