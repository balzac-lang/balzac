/*
 * Copyright 2021 Nicola Atzei
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

package xyz.balzaclang.generator

import com.google.inject.Inject
import java.io.File
import java.io.FileInputStream
import java.security.KeyStoreException
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.equinox.security.storage.ISecurePreferences
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.naming.IQualifiedNameProvider
import xyz.balzaclang.balzac.KeyLiteral
import xyz.balzaclang.balzac.Model
import xyz.balzaclang.balzac.PackageDeclaration
import xyz.balzaclang.lib.model.PrivateKey
import xyz.balzaclang.utils.ASTUtils
import xyz.balzaclang.utils.SecureStorageUtils
import xyz.balzaclang.lib.PrivateKeysStore

class KeyStoreGenerator extends AbstractGenerator {

    static final Logger logger = Logger.getLogger(KeyStoreGenerator);

    public static val KEYSTORE__FILENAME = "ks.p12"

    @Inject extension IQualifiedNameProvider
    @Inject(optional=true) ISecurePreferences secureStorage

    override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
        val models = resource.allContents.toIterable.filter(Model)
        val model = models.get(0)
        val packages = EcoreUtil2.getAllContentsOfType(model, PackageDeclaration)
        val packagePath =
            if (packages.isEmpty) {
                ""
            }
            else {
                var package = packages.get(0)
                package.fullyQualifiedName.toString(File.separator)
            }

        if (!(fsa instanceof InMemoryFileSystemAccess)) {
            logger.info("creating temporary keystore")
            val ksPath = createTempKeyStore(model)
            logger.info("copying temporary keystore to "+packagePath + File.separator + KEYSTORE__FILENAME)
            fsa.generateFile(packagePath + File.separator + KEYSTORE__FILENAME, new FileInputStream(ksPath))
        }
    }

    /**
     * Create a temporary keystore and populate it with private keys.
     * The alias of the entry is {@link ASTUtils#getUniqueID(KeyLiteral)}
     */
    def private String createTempKeyStore(Model model) {

        val keys = EcoreUtil2.getAllContentsOfType(model, KeyLiteral)

        try {
            val ecks = new PrivateKeysStore(getKsPassword())

            for (k : keys) {
                val key = PrivateKey.fromBase58(k.value)
                val alias = ecks.addKey(key)
                logger.info('''adding key with alias «alias»''')
            }

            val tmpFile = File.createTempFile("kstore", ".p12")
            ecks.store(tmpFile)
            return tmpFile.absolutePath
        }
        catch(KeyStoreException e) {
            e.printStackTrace
            throw new KeyStoreGenerationException('''Something went wrong generating the keystore: «e.message». Please report the error to the authors.''', e)
        }
    }

    /**
     * Retrieve the password for the generated keystore.
     * Firstly, it search for system property {@link #KEYSTORE__SECURE_STORAGE__PROPERTY_NAME}.
     * If not set, it search for the same property within node {@link #KEYSTORE__SECURE_STORAGE__NODE}
     * of Eclipse secure storage. The latter can be set by the UI module through preferences.
     */
    def private getKsPassword() {
        // System properties
        var pass = System.properties.getProperty(SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD)

        if (pass !== null) {
            logger.info("Reading password from system property "+SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD)
            return pass.toCharArray
        }

        // Eclipse secure storage
        if (secureStorage !== null) {
            val node = secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__KEYSTORE)
            pass = node.get(SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD, null)
            if (pass !== null) {
                logger.info("Reading password from secure storage")
                return pass.toCharArray
            }
        }

        logger.error('''Keystore password cannot be found. Specify it through system property -D«SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD» or plugin properties (it will be stored within Eclipse secure storage)''')
        throw new KeyStoreGenerationException('''Keystore password cannot be found. Specify it through system property -D«SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD» or plugin properties (it will be stored within Eclipse secure storage)''')
    }

    /*
     * Exception occurred during the keystore generation
     */
    static class KeyStoreGenerationException extends RuntimeException {
        new (String msg) { super(msg) }
        new (Throwable cause) { super(cause) }
        new (String msg, Throwable cause) { super(msg, cause) }
    }
}
