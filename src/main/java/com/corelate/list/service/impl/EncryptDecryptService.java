package com.corelate.list.service.impl;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class EncryptDecryptService {

    public static Map<String, Object> map = new HashMap<>();

    public String createKeys(){
        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("AES");
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            map.put("publicKey", publicKey);
            map.put("privateKey", privateKey);
            return new String("successfully created keys");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String("failed to create keys");
    }

    public String encryptMessage(String text) {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            PublicKey publicKey = (PublicKey) map.get("publicKey");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypt = cipher.doFinal(text.getBytes());
            return new String(Base64.getEncoder().encodeToString(encrypt));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "unable to encrypt";
    }

    public String decryptMessage(String text) {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            PrivateKey privateKey = (PrivateKey) map.get("privateKey");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decypt = cipher.doFinal(Base64.getDecoder().decode(text));
            return new String(decypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unable to decrypt";
    }
}
