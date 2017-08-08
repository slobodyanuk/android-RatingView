package com.skysofttech.ratingview;

import android.graphics.Path;
import android.graphics.RectF;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class RatingUtils {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 100000) return Long.toString(value);

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static long getMaxValue(long[] values) {
        long tmp = values[0];
        for (long value : values) {
            if (value > tmp) {
                tmp = value;
            }
        }
        return tmp;
    }

    public static double getTotalRating(long[] rates) {
        long countOf1 = rates[0];
        long countOf2 = rates[1];
        long countOf3 = rates[2];
        long countOf4 = rates[3];
        long countOf5 = rates[4];

        long countSum = countOf1 + countOf2 + countOf3 + countOf4 + countOf5;

        if (countSum <= 0) return 0.0f;

        return (double) (countOf1 + countOf2 * 2 + countOf3 * 3 + countOf4 * 4 + countOf5 * 5) / countSum;
    }

    public static Path roundRect(float topLeft, float topRight, float bottomRight, float bottomLeft, RectF rect) {
        final Path path = new Path();
        final float[] radii = new float[8];

        radii[0] = topLeft;
        radii[1] = topLeft;

        radii[2] = topRight;
        radii[3] = topRight;

        radii[4] = bottomRight;
        radii[5] = bottomRight;

        radii[6] = bottomLeft;
        radii[7] = bottomLeft;

        path.addRoundRect(rect, radii, Path.Direction.CW);

        return path;
    }

}