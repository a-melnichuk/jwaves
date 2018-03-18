package com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alexmelnichuk on 3/1/18.
 */

public class Util {
    public static void printByteArray(byte[] a) {
        System.out.println(Arrays.toString(a));
    }

    public static List<Integer> listOfUByte(byte[] a) {
        List<Integer> list = new ArrayList<>();
        for (byte b : a) {
            list.add(Byte.toUnsignedInt(b));
        }
        return list;
    }

    public static String hexString(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (byte b : a) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static void printHexString(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (byte b : a) {
            sb.append(String.format("%02X", b));
        }
        System.out.println(sb);
    }

    public static void printHexString(List<Integer> a) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : a) {
            sb.append(String.format("%02X", i));
        }
        System.out.println(sb);
    }

    public static void printUByteArray(byte[] a) {
        System.out.println(listOfUByte(a));
    }

}
