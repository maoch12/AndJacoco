package org.jacoco.core.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
    public static String MD5(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(s.getBytes("UTF8"));
        byte[] digest = md.digest();
        String result = "";
        for (int i = 0; i < digest.length; i++) {
            result += Integer.toHexString((0x000000FF & digest[i]) | 0xFFFFFF00).substring(6);
        }
        return result;
    }
}
