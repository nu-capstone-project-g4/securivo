package com.securivo.tools;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    public static List<byte[]> encodeFile(byte[] key, byte[] fileData) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encryptData = cipher.doFinal(fileData);
        byte[] iv = cipher.getIV();
        List<byte[]> encrypted = new ArrayList<>();
        encrypted.add(encryptData);
        encrypted.add(iv);
        return encrypted;
    }

    public static byte[] decodeFile(byte[] key, byte[] fileData, byte[] iv) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }
}
