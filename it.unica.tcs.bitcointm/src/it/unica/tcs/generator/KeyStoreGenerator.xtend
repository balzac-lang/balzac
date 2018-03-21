/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.KeyLiteral
import it.unica.tcs.bitcoinTM.Model
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.lib.ECKeyStore
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.SecureStorageUtils
import java.io.File
import java.io.FileInputStream
import java.security.KeyStoreException
import org.bitcoinj.core.DumpedPrivateKey
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.equinox.security.storage.ISecurePreferences
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.naming.IQualifiedNameProvider

class KeyStoreGenerator extends AbstractGenerator {

    public static val KEYSTORE__FILENAME = "ks.p12"

    @Inject private extension IQualifiedNameProvider
    @Inject private extension ASTUtils
    @Inject(optional=true) private ISecurePreferences secureStorage

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
println(fsa)
        if (!(fsa instanceof InMemoryFileSystemAccess)) {
            val ksPath = createTempKeyStore(model)
            fsa.generateFile(packagePath + File.separator + KEYSTORE__FILENAME, new FileInputStream(ksPath))
        }
    }

    /**
     * Create a temporary keystore and populate it with private keys.
     * The alias of the entry is {@link ASTUtils#getUniqueID(KeyLiteral)}
     */
    def private String createTempKeyStore(Model model) {

        val keys = EcoreUtil2.getAllContentsOfType(model, KeyLiteral).filter[k|k.isPrivateKey]

        try {
            val ecks = new ECKeyStore(getKsPassword())

            for (k : keys) {
                val key = DumpedPrivateKey.fromBase58(k.networkParams, k.value).key
            	val alias = ecks.addKey(key)
            	println('''adding key with alias «alias»''')
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
    		println("Reading password from system property "+SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD)
    	    return pass.toCharArray
    	}

    	// Eclipse secure storage
        if (secureStorage !== null) {
            val node = secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__KEYSTORE)
            pass = node.get(SecureStorageUtils.SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD, null)
            if (pass !== null) {
                println("Reading password from secure storage")
                return pass.toCharArray
            }
        }

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
