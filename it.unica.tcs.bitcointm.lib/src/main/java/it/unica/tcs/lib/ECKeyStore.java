/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    /**
     * Create a new ECKeyStore.
     * The keystore file is <b>temporary</b> and <b>the password is empty</b>.
     *
     * <p>Use methods {@link #changeKeyStoreFile(File, boolean)} and
     * {@link #changePassword(char[])} to set a new file and a new password.</p>
     *
     * <p><b>Don't add keys before invoking your password</b></p>
     * @return an instance of ECKeyStore
     * @throws KeyStoreException if an error occur creating the {@link KeyStore}
     */
    public static ECKeyStore create() throws KeyStoreException {
        try {
            KeyStore ks = KeyStore.getInstance("pkcs12");
            File ksFile = File.createTempFile("keystore-tmp", ".p12");
            ks.load(null);
            ks.store(new FileOutputStream(ksFile), new char[0]);
            return new ECKeyStore(ksFile);
        }
        catch(KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException(e);
        }
    }

    private File ksFile;
    private char[] password;
    private KeyStore ks;

    public ECKeyStore(String keystorePath) throws KeyStoreException {
        this(keystorePath, new char[0]);
    }

    public ECKeyStore(String keystorePath, char[] password) throws KeyStoreException {
        this(new File(keystorePath), password);
    }

    public ECKeyStore(File keystoreFile) throws KeyStoreException {
        this(keystoreFile, new char[0]);
    }

    public ECKeyStore(File keystoreFile, char[] password) throws KeyStoreException {
        checkNotNull(keystoreFile);
        checkArgument(keystoreFile.isFile(), "Cannot find file "+keystoreFile.getAbsolutePath());
        checkArgument(keystoreFile.canRead(), "Cannot have read permission to file "+keystoreFile.getAbsolutePath());
        checkArgument(keystoreFile.canWrite(), "Cannot have write permission to file "+keystoreFile.getAbsolutePath());
        this.ksFile = keystoreFile;
        this.password = Arrays.copyOf(password, password.length);
        this.ks = KeyStore.getInstance("pkcs12");
        init();
    }

    private void init() throws KeyStoreException {
        try (FileInputStream fis = new FileInputStream(ksFile)) {
            ks.load(fis, password);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new KeyStoreException("Cannot load keystore "+ksFile.getAbsolutePath()+": "+e.getMessage(), e);
        }
    }

    public File getKeyStoreFile() {
        return ksFile;
    }

    public KeyStore getKeyStore() {
        return ks;
    }

    public String addKey(ECKey key) throws KeyStoreException {
        checkState(!key.isPubKeyOnly(), "Only private key are allowed.");
        KeyStore ks = getKeyStore();
        String alias = getUniqueID(key);
        SecretKey secretKey = new SecretKeySpec(key.getPrivKeyBytes(), "EC");
        SecretKeyEntry kEntry = new SecretKeyEntry(secretKey);
        ks.setEntry(alias, kEntry, new PasswordProtection(password));
        flush();
        return alias;
    }

    public ECKey getKey(String keyID) throws KeyStoreException {
        KeyStore ks = getKeyStore();
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

    public void flush() throws KeyStoreException {
        try (FileOutputStream fos = new FileOutputStream(ksFile)) {
            ks.store(fos, password);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new KeyStoreException("Cannot store keystore "+ksFile.getAbsolutePath()+": "+e.getMessage(), e);
        }
    }

    public boolean contains(String keyID) throws KeyStoreException {
        return getKeyStore().containsAlias(keyID);
    }

    public void changeKeyStoreFile(File ksFile, boolean deleteOld) throws KeyStoreException{
        File tmp = this.ksFile;
        this.ksFile = ksFile;
        flush();
        if (deleteOld)
            tmp.delete();
    }

    public void changePassword(char[] password) throws KeyStoreException {
        flush();
        ECKeyStore tmp = ECKeyStore.create();

        try {
            // populate the temporary store
            for (String alias : Collections.list(ks.aliases())) {
                Entry entry = ks.getEntry(alias, new PasswordProtection(this.password));
                tmp.ks.setEntry(alias, entry, new PasswordProtection(password));    // new password
            }

            // update the password
            Arrays.fill(this.password, '0');
            this.password = Arrays.copyOf(password, password.length);

            // override the previous keystore file
            this.ks = tmp.ks;
            flush();

            // cleanup
            tmp.ksFile.delete();
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new KeyStoreException(e);
        }
    }
}
