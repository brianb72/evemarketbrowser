package com.troff.evemarketbrowser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

// TODO  java already has a "NumberFormat" class, what should our class be renamed to?

public class NumberFormatter {

    // From http://stackoverflow.com/a/30661479/1316642
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "Q");
        //suffixes.put(1_000_000_000_000_000_000L, "Q");
    }
    public static String shrinkWithSuffix(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return shrinkWithSuffix(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + shrinkWithSuffix(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }


    // Only show decimal values on numbers under 100, truncate over 100
    public static String formatISKValue(BigDecimal value) {
        BigDecimal bd100 = new BigDecimal(100);

        if (value.compareTo(new BigDecimal(100)) == -1) {
            return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

}
