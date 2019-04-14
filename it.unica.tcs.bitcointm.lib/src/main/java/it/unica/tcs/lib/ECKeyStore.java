/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;

import it.unica.tcs.lib.utils.BitcoinUtils;

public class ECKeyStore {

    public static String getUniqueID(ECKey key) {
        return BitcoinUtils.encode(Utils.sha256hash160(key.getPrivKeyBytes()));
    }

    public static String getUniqueID(String wif) {
        return getUniqueID(DumpedPrivateKey.fromBase58(null, wif).getKey());
    }

    private char[] password;
    private KeyStore ks;

    /**
     * Create a new ECKeyStore with an <b>empty password</b>.
     * Use {@link #changePassword(char[])} to set a new password.
     * @return an instance of ECKeyStore
     * @throws KeyStoreException if an error occur creating the {@link KeyStore}
     */
    public ECKeyStore() throws KeyStoreException {
        this(new char[0]);
    }

    /**
     * Create a new ECKeyStore with the specified password.
     * The same password is used to store the keystore via {@link #store(File)} and for entries.
     * Use {@link #changePassword(char[])} to set a new password.
     * @param password a password for the store and its entries.
     * @return an instance of ECKeyStore.
     * @throws KeyStoreException if an error occur creating the {@link KeyStore}
     */
    public ECKeyStore(char[] password) throws KeyStoreException {
        this(null, password);
    }

    /**
     * Load the keystore from the given input stream, or create a new one if null.
     * The password is used to decrypt the keystore <b>and each entry</b>.
     * @param input the input stream from which load the keystore.
     * @param password a password for the store and its entries.
     * @throws KeyStoreException
     */
    public ECKeyStore(InputStream input, char[] password) throws KeyStoreException {
        this.password = Arrays.copyOf(password, password.length);
        this.ks = KeyStore.getInstance("pkcs12");
        try {
            ks.load(input, password);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new KeyStoreException("Cannot create the keystore: "+e.getMessage(), e);
        }
    }

    public String addKey(String wif) throws KeyStoreException {
        return addKey(DumpedPrivateKey.fromBase58(null, wif).getKey());
    }

    public String addKey(ECKey key) throws KeyStoreException {
        checkState(!key.isPubKeyOnly(), "Only private key are allowed.");
        String alias = getUniqueID(key);
        SecretKey secretKey = new SecretKeySpec(key.getPrivKeyBytes(), "EC");
        SecretKeyEntry kEntry = new SecretKeyEntry(secretKey);
        ks.setEntry(alias, kEntry, new PasswordProtection(password));
        return alias;
    }

    public ECKey getKey(String keyID) throws KeyStoreException {
        checkState(ks.containsAlias(keyID));
        Key entryKey;
        try {
            entryKey = ks.getKey(keyID, password);
            ECKey key = ECKey.fromPrivate(entryKey.getEncoded());
            return key;
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new KeyStoreException("Cannot fetch key "+keyID+": "+e.getMessage(), e);
        }
    }

    public boolean containsKey(String keyID) throws KeyStoreException {
        return ks.containsAlias(keyID);
    }

    public void store(File ksFile) throws KeyStoreException {
        try (FileOutputStream fos = new FileOutputStream(ksFile)) {
            ks.store(fos, password);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new KeyStoreException("Cannot store keystore "+ksFile.getAbsolutePath()+": "+e.getMessage(), e);
        }
    }

    public void changePassword(char[] password) throws KeyStoreException {
        try {
            for (String alias : Collections.list(ks.aliases())) {
                Entry entry = ks.getEntry(alias, new PasswordProtection(this.password));    // read
                ks.setEntry(alias, entry, new PasswordProtection(password));                // override
            }

            // update the password
            Arrays.fill(this.password, '0');
            this.password = Arrays.copyOf(password, password.length);

        } catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new KeyStoreException(e);
        }
    }
}
