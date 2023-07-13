package com.fiercemanul.blackholestorage.util;

import java.text.DecimalFormat;
import java.util.Map;

public class Tools {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");

    public static int sortItemFromCount(String s1, String s2, Map<String, Integer> storageItems, boolean reverseOrder) {
        int i;
        if (reverseOrder) {
            i = storageItems.get(s2).compareTo(storageItems.get(s1));
        } else {
            i = storageItems.get(s1).compareTo(storageItems.get(s2));
        }
        if (i == 0) i = s1.compareTo(s2);
        return i;
    }

    public static int sortItemFromRightID(String s1, String s2) {
        int i = s1.indexOf(":");
        String a = s1.substring(i + 1);
        int j = s2.indexOf(":");
        String b = s2.substring(j + 1);
        int k = a.compareTo(b);
        if (k == 0) k = s1.compareTo(s2);
        return k;
    }

    public static int sortItemFromMirrorID(String s1, String s2) {
        char[] a = s1.toCharArray();
        char[] b = s2.toCharArray();
        int j = a.length - 1;
        int k = b.length - 1;
        int l;
        int min = Math.min(a.length, b.length);
        for (int i = 0; i < min; i++) {
            l = Character.compare(a[j], b[k]);
            if (l != 0) return l;
            j--; k--;
        }
        return Integer.compare(a.length, b.length);
    }
}
