/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.utils;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;


@Component
public class SignatureUtil {

    private final AppConfigs appConfigs;
    private final static Logger logger = LoggerFactory.getLogger(SignatureUtil.class);

    public SignatureUtil(AppConfigs appConfigs) {
        this.appConfigs = appConfigs;
    }

    public String sign(String plainText){
        PrivateKey key = Objects.requireNonNull(this.loadCertificates()).getPrivate();
        try{
            //Let's sign our message
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(key);
            privateSignature.update(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] signature = privateSignature.sign();
            return Base64.encodeBase64String(signature);
        }catch (Exception ex){
            logger.error("An error has occurred while signing the message");
        }return null;
    }

    public boolean verify(String plainText, String signature) {
        try{
            PublicKey publicKey = Objects.requireNonNull(this.loadCertificates()).getPublic();
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(plainText.getBytes(UTF_8));
            byte[] signatureBytes = Base64.decodeBase64(signature);
            return publicSignature.verify(signatureBytes);
        }catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return false;
    }

    public KeyPair loadCertificates(){
        String password = appConfigs.getCertificatePassword();
        try {
            FileInputStream is = new FileInputStream(Paths.get(appConfigs.getCertificatePath()).toFile());
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, password.toCharArray());
            String alias = appConfigs.getCertificateAlias();
            Key key = keystore.getKey(alias, password.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keystore.getCertificate(alias);
                // Get public key
                PublicKey publicKey = cert.getPublicKey();
                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 CertificateException ex) {
            logger.error("An error occurred while " + ex.getMessage());
        }
        return null;
    }
}
