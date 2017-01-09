package com.imobiapp.parser.dm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author matics.
 */
public class AdData {
    public String url;
    public String price;
    public String size;
    public String summary;
    public String shortDescripton;
    public String longDescription;
    public String contact;

    public String calculateHash() {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String message = price + size + summary + shortDescripton + longDescription + contact;
        byte[] hashBytes = digest.digest(message.getBytes());

        return convertByteArrayToHexString(hashBytes);
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                                       .substring(1));
        }
        return stringBuffer.toString();
    }

}
