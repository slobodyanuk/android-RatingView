package com.skysofttech.ratingview;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.TextView;

/**
 * Author: Serhii Slobodianiuk
 * Date: 7/28/17
 */

public class Utils {

    public static int arraySum(int[] array){
        int sum = 0;
        for (int element : array){
            sum += element;
        }
        return sum;
    }

    public static int[] getMinTextSize(Context context, int number, float textSize) {
        TextView textView = new TextView(context);
        textView.setTextSize(textSize);
        Rect bounds = new Rect();
        Paint textPaint = textView.getPaint();
        textPaint.setTextSize(textSize);
        String text = String.valueOf(number);
        textPaint.getTextBounds(text, 0, text.length(), bounds);
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

    public static float fitTextSize(TextPaint textPaint, String text, int parentWidth, int parentHeight, float size) {
        StaticLayout textLayout = new StaticLayout(text, textPaint, parentWidth, Layout.Alignment.ALIGN_CENTER,
                1, 1, false);

        int width = textLayout.getWidth();
        int height = textLayout.getHeight();

        while (width > parentWidth * 2 || height > parentHeight * 2) {
            textPaint.setTextSize(textPaint.getTextSize() - 1);
            textLayout = new StaticLayout(text, textPaint, parentWidth, Layout.Alignment.ALIGN_CENTER,
                    1, 1, false);
            width = textLayout.getWidth();
            height = textLayout.getHeight();
        }

        return textPaint.getTextSize() / size;
    }


}
