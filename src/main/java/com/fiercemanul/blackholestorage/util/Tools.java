package com.fiercemanul.blackholestorage.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class Tools {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");

    public static String[] sortItemFromCount(Map<String, Integer> storageItems, boolean reverseOrder) {
        String[] keys = storageItems.keySet().toArray(new String[0]);
        if (reverseOrder) Arrays.parallelSort(keys, (s1, s2) -> {
            int i = storageItems.get(s2).compareTo(storageItems.get(s1));
            if (i == 0) i = s1.compareTo(s2);
            return i;
        });
        else Arrays.parallelSort(keys, Comparator.comparingInt((String s) -> storageItems.get(s)).thenComparing(s -> s));
        return keys;
    }

}
