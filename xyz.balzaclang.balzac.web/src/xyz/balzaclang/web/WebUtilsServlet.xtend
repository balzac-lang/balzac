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
package xyz.balzaclang.web

import com.google.gson.Gson
import com.google.gson.JsonParseException
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException
import org.apache.commons.io.IOUtils
import org.eclipse.xtend.lib.annotations.Accessors
import xyz.balzaclang.lib.model.Address
import xyz.balzaclang.lib.model.Hash
import xyz.balzaclang.lib.model.Hash.HashAlgorithm
import xyz.balzaclang.lib.model.NetworkType
import xyz.balzaclang.lib.model.PrivateKey

@WebServlet(name = 'WebUtils', urlPatterns = '/api/*')
class WebUtilsServlet extends HttpServlet {

    val gson = new Gson

    @Accessors
    static class KeyResult {
        val String privkeyTestnet
        val String privkeyMainnet
        val String pubkey
        val String addressTestnet
        val String addressMainnet
    }

    @Accessors
    static class HashRequest {
        val String value
        val boolean hashAsString
    }

    @Accessors
    static class HashResult {
        val String value
        val String type
        val String sha1
        val String sha256
        val String ripemd160
        val String hash256
        val String hash160
    }

    override protected doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        if (req.requestURI.contains("/api/keys")) {
            val key = PrivateKey.fresh(NetworkType.TESTNET)
            val privkeyTestnet = key.wif
            val privkeyMainnet = key.withNetwork(NetworkType.MAINNET).wif
            val publickey = key.toPublicKey.bytesAsString
            val addressTestnet = Address.from(key.toPublicKey, NetworkType.TESTNET).wif
            val addressMainnet = Address.from(key.toPublicKey, NetworkType.MAINNET).wif
            val result = new KeyResult(privkeyTestnet, privkeyMainnet, publickey, addressTestnet, addressMainnet)
            response.contentType = 'application/json'
            response.status = HttpServletResponse.SC_OK
            gson.toJson(result, response.writer)
            return
        }
        else if (req.requestURI.contains("/api/hash") && "application/json".equalsIgnoreCase(req.contentType)) {
            response.contentType = 'application/json'
            response.status = HttpServletResponse.SC_OK

            try {
                val hashRequest = gson.fromJson(req.reader, HashRequest)

                if (hashRequest === null) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    IOUtils.write("Missing request body\n", response.writer)
                    return
                }

                val valueToHash = hashRequest.value
                val hashAsString = hashRequest.hashAsString

                // Parameter check
                if (valueToHash === null) {
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    IOUtils.write("Missing field 'value'\n", response.writer)
                    return
                }

                if (hashAsString) {
                    gson.toJson(hash(valueToHash), response.writer)
                    return
                }

                try {
                    /*
                     * Try to interpret value as Integer
                     */
                    gson.toJson(hash(Long.parseLong(valueToHash)), response.writer)
                    return
                }
                catch (NumberFormatException e) {
                    /*
                     * Try to interpret true/false
                     */
                    if (valueToHash.toLowerCase == "true") {
                        gson.toJson(hash(true), response.writer)
                        return
                    }

                    if (valueToHash.toLowerCase == "false") {
                        gson.toJson(hash(false), response.writer)
                        return
                    }

                    /*
                     * Finally treat it as a String
                     */
                    gson.toJson(hash(valueToHash), response.writer)
                    return
                }

            }
            catch (JsonParseException e) {
                response.status = HttpServletResponse.SC_BAD_REQUEST
                IOUtils.write("Unable to parse the JSON body\n", response.writer)
                return
            }
            catch (RuntimeException e) {
                response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                IOUtils.write("An internal server error occurred. Please report to the authors.\n", response.writer)
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
