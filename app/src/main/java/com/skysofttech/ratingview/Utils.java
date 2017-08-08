package com.skysofttech.ratingview;

import android.graphics.Rect;
import android.text.TextPaint;

/**
 * Author: Serhii Slobodianiuk
 * Date: 7/28/17
 */

public class Utils {


    public static int arraySum(long[] array) {
        int sum = 0;
        for (long element : array) {
            sum += element;
        }
        return sum;
    }

    public static int[] getMinTextSize(long number, float textSize) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        String text = String.valueOf(number);
        paint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.height();
        int width = bounds.width();
        return new int[]{width, height};
    }


    public static int getMaxValue(int[] values) {
        int tmp = values[0];
        for (int value : values) {
            if (value > tmp) {
                tmp = value;
            }
        }
        return tmp;
    }



}
