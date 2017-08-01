package com.skysofttech.ratingview;

public class RatingUtils {

    public static float getTotalRating(int[] rates) {
        int countOf1 = rates[0];
        int countOf2 = rates[1];
        int countOf3 = rates[2];
        int countOf4 = rates[3];
        int countOf5 = rates[4];

        int countSum = countOf1 + countOf2 + countOf3 + countOf4 + countOf5;

        if (countSum <= 0) return 0.0f;

        return (float) (countOf1 + countOf2 * 2 + countOf3 * 3 + countOf4 * 4 + countOf5 * 5) / countSum;
    }

}