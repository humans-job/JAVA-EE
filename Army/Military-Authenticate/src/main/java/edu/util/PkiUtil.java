package edu.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

public class PkiUtil {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static KeyPair genRsaKeyPair(int bits) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(bits);
            return kpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("生成KeyPair失败: " + e.getMessage(), e);
        }
    }

    /** 生成自建CA证书（CA=true，自签） */
    public static X509Certificate createCaCert(KeyPair caKeyPair, String subjectDn, int validDays) {
        try {
            X500Name dn = new X500Name(subjectDn);
            BigInteger serial = new BigInteger(128, new SecureRandom()).abs();

            Instant now = Instant.now();
            Date notBefore = Date.from(now.minus(1, ChronoUnit.MINUTES));
            Date notAfter  = Date.from(now.plus(validDays, ChronoUnit.DAYS));

            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                    dn, serial, notBefore, notAfter, dn, caKeyPair.getPublic()
            );

            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
            builder.addExtension(Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(caKeyPair.getPrivate());

            X509CertificateHolder holder = builder.build(signer);
            X509Certificate cert = new JcaX509CertificateConverter()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate(holder);

            cert.verify(caKeyPair.getPublic());
            return cert;
        } catch (Exception e) {
            throw new RuntimeException("生成CA证书失败: " + e.getMessage(), e);
        }
    }

    /** CA 签发用户证书（issuer=CA，CA=false） */
    public static X509Certificate issueUserCert(
            PublicKey userPublicKey,
            String userSubjectDn,
            PrivateKey caPrivateKey,
            X509Certificate caCert,
            BigInteger serial,
            int validDays
    ) {
        try {
            X500Name subject = new X500Name(userSubjectDn);
            X500Name issuer = X500Name.getInstance(caCert.getSubjectX500Principal().getEncoded());

            Instant now = Instant.now();
            Date notBefore = Date.from(now.minus(1, ChronoUnit.MINUTES));
            Date notAfter  = Date.from(now.plus(validDays, ChronoUnit.DAYS));

            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                    issuer, serial, notBefore, notAfter, subject, userPublicKey
            );

            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
            builder.addExtension(Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
            builder.addExtension(Extension.extendedKeyUsage, false,
                    new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(caPrivateKey);

            X509CertificateHolder holder = builder.build(signer);
            X509Certificate userCert = new JcaX509CertificateConverter()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate(holder);

            userCert.verify(caCert.getPublicKey());
            return userCert;
        } catch (Exception e) {
            throw new RuntimeException("签发用户证书失败: " + e.getMessage(), e);
        }
    }

    public static byte[] toPkcs12(String alias, PrivateKey privateKey, X509Certificate[] chain, String password) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.setKeyEntry(alias, privateKey, password.toCharArray(), chain);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                ks.store(bos, password.toCharArray());
                return bos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("导出P12失败: " + e.getMessage(), e);
        }
    }

    public static String certToPem(X509Certificate cert) {
        try {
            String b64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(cert.getEncoded());
            return "-----BEGIN CERTIFICATE-----\n" + b64 + "\n-----END CERTIFICATE-----\n";
        } catch (Exception e) {
            throw new RuntimeException("证书转PEM失败: " + e.getMessage(), e);
        }
    }

    public static String serialHex(BigInteger serial) {
        return serial.toString(16).toUpperCase();
    }
}

