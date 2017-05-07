package pt.up.fc.dcc.mousetrap.utils;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import pt.up.fc.dcc.mousetrap.App;
import pt.up.fc.dcc.mousetrap.Constants;

/**
 * Utils for connecting with SSL
 */
public class SslUtils {

    public static SSLSocketFactory getSocketFactory(final int caCrtFile, final int crtFile,
                                                    final int keyStoreFile, String alias,
                                                    String password) {

        if (alias == null)
            alias = Constants.DEFAULT_ALIAS;

        if (password == null)
            password = Constants.DEFAULT_PASSWORD;

        try {

            // Add BouncyCastle as a Security Provider
            Security.addProvider(new BouncyCastleProvider());

            JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

            // Load Certificate Authority (CA) certificate
            PEMParser reader = new PEMParser(new InputStreamReader(App.getContext().getResources().openRawResource(caCrtFile)));
            X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
            reader.close();

            X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);

            // Load client certificate
            reader = new PEMParser(new InputStreamReader(App.getContext().getResources().openRawResource(crtFile)));
            X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
            reader.close();

            X509Certificate cert = certificateConverter.getCertificate(certHolder);

            // Load client private key
            KeyStore keystore = KeyStore.getInstance("BKS");
            keystore.load(App.getContext().getResources().openRawResource(keyStoreFile), password.toCharArray());

            KeyPair keyPair = null;
            Key key = keystore.getKey(alias, password.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate publicKeyCert = keystore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                // Get a key pair
                keyPair = new KeyPair(publicKey, (PrivateKey) key);
            }

            // CA certificate is used to authenticate server
            KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            caKeyStore.load(null, null);
            caKeyStore.setCertificateEntry("ca-certificate", caCert);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(caKeyStore);

            // Client key and certificates are sent to server so it can authenticate the client
            KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            clientKeyStore.load(null, null);
            clientKeyStore.setCertificateEntry("certificate", cert);
            clientKeyStore.setKeyEntry("private-key", keyPair.getPrivate(), password.toCharArray(),
                    new Certificate[]{cert});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());

            // Create SSL socket factory
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Return the newly created socket factory object
            return context.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
