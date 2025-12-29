package org.example.army.militaryauthenticate.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Component
public class CaStore {

    @Value("${pki.ca.keystore-path}")
    private String keystorePath;

    @Value("${pki.ca.keystore-password}")
    private String keystorePassword;

    @Value("${pki.ca.alias}")
    private String alias;

    public boolean exists() {
        return new File(keystorePath).exists();
    }

    public void save(PrivateKey caPrivateKey, X509Certificate caCert) {
        try {
            File file = new File(keystorePath);
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.setKeyEntry(alias, caPrivateKey, keystorePassword.toCharArray(),
                    new java.security.cert.Certificate[]{caCert});

            try (FileOutputStream fos = new FileOutputStream(file)) {
                ks.store(fos, keystorePassword.toCharArray());
            }
        } catch (Exception e) {
            throw new RuntimeException("保存CA keystore失败: " + e.getMessage(), e);
        }
    }

    public LoadedCa load() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                ks.load(fis, keystorePassword.toCharArray());
            }
            PrivateKey caKey = (PrivateKey) ks.getKey(alias, keystorePassword.toCharArray());
            X509Certificate caCert = (X509Certificate) ks.getCertificate(alias);
            if (caKey == null || caCert == null) {
                throw new RuntimeException("CA keystore内容不完整");
            }
            return new LoadedCa(caKey, caCert);
        } catch (Exception e) {
            throw new RuntimeException("加载CA keystore失败: " + e.getMessage(), e);
        }
    }

    public record LoadedCa(PrivateKey privateKey, X509Certificate certificate) {}
}
