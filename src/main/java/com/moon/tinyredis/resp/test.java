package com.moon.tinyredis.resp;

import java.io.UnsupportedEncodingException;

public class test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String  str = "name";
        byte[] bytes = str.getBytes("utf-8");
        for (byte aByte : bytes) {
            System.out.println(aByte);
        }
    }
}
