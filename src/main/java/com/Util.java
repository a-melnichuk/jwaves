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

    public static void printUByteArray(byte[] a) {
        System.out.println(listOfUByte(a));
    }

}
