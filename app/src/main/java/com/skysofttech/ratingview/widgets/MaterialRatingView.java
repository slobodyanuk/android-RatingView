package com.skysofttech.ratingview.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.Px;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;

import com.skysofttech.ratingview.R;
import com.skysofttech.ratingview.RatingUtils;
import com.skysofttech.ratingview.Utils;
import com.thefinestartist.utils.ui.DisplayUtil;

/**
 * Author: Serhii Slobodianiuk
 * Date: 7/27/17
 */

public class MaterialRatingView extends View {

    private static final String TAG = MaterialRatingView.class.getCanonicalName();

    private static final float DEFAULT_BAR_WIDTH = 0.65f;

    private static final int DEFAULT_BAR_CONTAINER_PADDING = R.dimen.barContainerPadding;
    private static final int DEFAULT_BAR_PADDING = R.dimen.barPadding;
    private static final int DEFAULT_TEXT_PADDING = R.dimen.textPadding;
    private static final int DEFAULT_FRAMES_PER_SECOND = 60;

    private static final int DEFAULT_ANIMATION_DURATION = 1500;

    private static final int SPEED_PER_SECOND = 15;

    private final LayoutInflater inflater;

    @ColorInt
    private int barColor = R.color.colorAccent;

    @ColorInt
    private int numbersColor = android.R.color.black;

    private float numbersSize;

    @ColorInt
    private int totalCountColor = R.color.colorAccent;

    @DrawableRes
    private int drawableStar = android.R.drawable.star_on;

    private long[] countStars = {0, 0, 0, 0, 0};
    private boolean animated;

    @FloatRange(from = 0.0f, to = 1.0f)
    private float barWidthMultiplier;

    private float barWidth;

    @Px
    private int barContainerPadding;

    @Px
    private int barPadding;

    private int textPadding;

    private Context context;

    private boolean draw = false;

    private TextPaint numberPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint ratingTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint ratingCountPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ratingBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint totalBarPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint reviewsBarPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private Rect numberBounds = new Rect();
    private Rect ratingTextBounds = new Rect();
    private Rect reviewsTextBounds = new Rect();
    private Rect totalTextBounds = new Rect();

    private int maxNumberWidth;
    private float ratingMaxWidth;

    private Bitmap starBitmap;

    private int width;
    private int height;
    private int barHeight;

    private int minTextWidth;

    private int progress;
    private long maxRatingValue;

    private long beforeTime;
    private long timeThisFrame;

    private String reviewsText;

    public MaterialRatingView(Context context) {
        super(context);
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public MaterialRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setupAttribute(attrs);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public MaterialRatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setupAttribute(attrs);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    private void setupAttribute(AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.MaterialRatingView, 0, 0);

        barColor = attributes.getColor(R.styleable.MaterialRatingView_barColor,
                context.getResources().getColor(R.color.colorAccent));

        numbersColor = attributes.getColor(R.styleable.MaterialRatingView_numbersColor,
                context.getResources().getColor(android.R.color.black));
        numbersSize = attributes.getFloat(R.styleable.MaterialRatingView_numbersSize,
                context.getResources().getDimensionPixelSize(R.dimen.numbersSize));

        totalCountColor = attributes.getColor(R.styleable.MaterialRatingView_totalCountColor,
                context.getResources().getColor(R.color.colorAccent));

        drawableStar = attributes.getInt(R.styleable.MaterialRatingView_drawableStar,
                android.R.drawable.star_on);

        barWidthMultiplier = attributes.getFloat(R.styleable.MaterialRatingView_barWidth, DEFAULT_BAR_WIDTH);

        barContainerPadding = attributes.getDimensionPixelSize(R.styleable.MaterialRatingView_barContainerPadding,
                context.getResources().getDimensionPixelSize(DEFAULT_BAR_CONTAINER_PADDING));

        barPadding = attributes.getDimensionPixelSize(R.styleable.MaterialRatingView_barPadding,
                context.getResources().getDimensionPixelSize(DEFAULT_BAR_PADDING));

        initPaints();

        attributes.recycle();
    }

    private void initPaints() {
        //numbers of stars
        numberPaint.setColor(numbersColor);
        numberPaint.setTextAlign(Paint.Align.LEFT);
        numberPaint.setTextSize(numbersSize);

        ratingCountPaint.setColor(numbersColor);
        ratingCountPaint.setTextSize(numbersSize);

        ratingTextPaint.setTextSize(numbersSize);

        totalBarPaint.setColor(totalCountColor);
        reviewsBarPaint.setColor(numbersColor);

        //stars bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        starBitmap = BitmapFactory.decodeResource(getResources(), drawableStar, options);

        //rating bar
        ratingBarPaint.setColor(barColor);
        ratingBarPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = DisplayUtil.getWidth();
        int desiredHeight = (starBitmap.getHeight()) * countStars.length;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = desiredHeight;
        }

        barHeight = height / countStars.length;
        barWidth = width * barWidthMultiplier;

        numberPaint.getTextBounds(String.valueOf(countStars.length), 0, String.valueOf(countStars.length).length(), numberBounds);
        maxNumberWidth = numberBounds.width();
        textPadding = context.getResources().getDimensionPixelOffset(DEFAULT_TEXT_PADDING);

        maxRatingValue = RatingUtils.getMaxValue(countStars);

        reviewsText = getResources().getString(R.string.reviews, String.valueOf(RatingUtils.format(Utils.arraySum(countStars))));

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawViews(canvas);
    }

    private void drawViews(Canvas canvas) {
        for (int i = countStars.length - 1; i >= 0; i--) {
            drawNumber(canvas, i);
        }
    }

    private void drawNumber(Canvas canvas, int index) {
        String number = String.valueOf(index + 1);

        int xPos = barContainerPadding;
        int yPos = barHeight * (index + 1);
        int yTextCenter = (barHeight - numberBounds.height()) / 2;

        canvas.drawText(number, xPos, yPos - yTextCenter, numberPaint);

        drawStar(canvas, index);
    }

    private void drawStar(Canvas canvas, int index) {
        canvas.drawBitmap(starBitmap, maxNumberWidth + barContainerPadding * 1.5f,
                barHeight * (index) + (barHeight - starBitmap.getHeight()) / 2, starPaint);

        drawRatingBar(canvas, index);
    }

    private void drawRatingBar(Canvas canvas, int index) {

        float left = barPadding + maxNumberWidth + barContainerPadding * 1.5f + starBitmap.getWidth();
        float top = barPadding + index * barHeight;

        ratingMaxWidth = barWidth - left;
        float right = (maxRatingValue > 0f)
                ? left + (countStars[index] * ratingMaxWidth) / maxRatingValue
                : left;

        float bottom = barHeight - barPadding + top;

        RectF rect = new RectF(left, top, (animated && progress < right) ? progress : right, bottom);
        canvas.drawPath(RatingUtils.roundRect(0, barHeight * 0.2f, barHeight * 0.2f, 0, rect), ratingBarPaint);

        timeThisFrame = System.currentTimeMillis() - beforeTime;

        if (animated && index == 0 && progress <= ratingMaxWidth + left) {
            final float shift = SPEED_PER_SECOND * timeThisFrame / 1000;
            progress = (int) (progress + shift);
            postInvalidate();
        } else if (!animated || progress >= ratingMaxWidth + left) {
            drawRatingCounts(canvas, index, left, right);
            if (index == 0) {
                drawTotalRating(canvas);
            }
        }
    }

    private void drawRatingCounts(Canvas canvas, int index, float barLeft, float barRight) {
        int xPos;
        int yPos;
        int yTextCenter;

        minTextWidth = Utils.getMinTextSize(countStars[index], ratingCountPaint.getTextSize())[0];

        if (barRight - barLeft + minTextWidth + textPadding >= ratingMaxWidth) {
            xPos = (int) (barRight - minTextWidth - textPadding);
        } else {
            xPos = (int) (barRight + textPadding);
        }

        yPos = barHeight * (index + 1);
        ratingTextPaint.getTextBounds(String.valueOf(countStars[index]), 0, String.valueOf(countStars[index]).length(), ratingTextBounds);
        yTextCenter = (barHeight - ratingTextBounds.height()) / 2;

        canvas.drawText(String.valueOf(countStars[index]), xPos, yPos - yTextCenter, ratingCountPaint);
    }

    private void drawTotalRating(Canvas canvas) {

        double rating = Math.round(RatingUtils.getTotalRating(countStars) * 100.0) / 100.0;

        int totalWidth = (int) (width - barWidth);

        float multiplier = (float) Math.min(totalWidth, height) / (float) Math.max(totalWidth, height);

        float size = multiplier * totalWidth / getResources().getDisplayMetrics().density;

        totalBarPaint.setTextSize(size);

        totalBarPaint.getTextBounds(String.valueOf(rating), 0, String.valueOf(rating).length(), totalTextBounds);

        int xPos = (int) (barWidth + (totalWidth - totalTextBounds.width()) / 2);
        int yPos = height / 2;
        float yTextCenter = totalTextBounds.height() / 2;

        canvas.drawText(String.valueOf(rating), xPos, yPos - yTextCenter, totalBarPaint);

        View view = inflater.inflate(R.layout.layout_total, null);
        view.measure(totalWidth, height);
        view.layout(0, 0, totalWidth, height);

        RatingBar bar = view.findViewById(R.id.total_rating_bar);
        bar.setNumStars(countStars.length);
        bar.setStepSize(0.1f);
        bar.setRating((float) rating);

        LayerDrawable stars = (LayerDrawable) bar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(totalCountColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.grey_hex_a0), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.grey_hex_a0), PorterDuff.Mode.SRC_ATOP);

        view.layout(0, 0, totalWidth, height);

        canvas.save();
        canvas.translate(barWidth, 10);
        view.draw(canvas);
        canvas.restore();

        reviewsBarPaint.setTextSize(size / 3);

        reviewsBarPaint.getTextBounds(String.valueOf(reviewsText), 0, String.valueOf(reviewsText).length(), reviewsTextBounds);

        xPos = (int) (barWidth + (totalWidth - reviewsTextBounds.width()) / 2);
        yPos = (int) (bar.getY() + bar.getMeasuredHeight() + reviewsTextBounds.height() * 2);

        canvas.drawText(String.valueOf(reviewsText), xPos, yPos, reviewsBarPaint);

    }

    public int getNumbersColor() {
        return numbersColor;
    }

    public int getBarColor() {
        return barColor;
    }

    public int getTotalCountColor() {
        return totalCountColor;
    }

    public int getDrawableStar() {
        return drawableStar;
    }

    public Builder newBuilder() {
        return new Builder();
    }

    public class Builder {

        private long startTime;

        private Builder() {
        }

        public void setBarColor(int barColor) {
            MaterialRatingView.this.barColor = barColor;
        }


        public Builder setNumbersColor(int numbersColor) {
            MaterialRatingView.this.numbersColor = numbersColor;
            return this;
        }


        public Builder setTotalCountColor(int totalCountColor) {
            MaterialRatingView.this.totalCountColor = totalCountColor;
            return this;
        }

        public Builder setCountStars(long[] countStars) {
            MaterialRatingView.this.countStars = countStars;
            return this;
        }

        public Builder setDrawableStar(int drawableStar) {
            MaterialRatingView.this.drawableStar = drawableStar;
            return this;
        }

        public Builder setAnimated(boolean animated) {
            MaterialRatingView.this.animated = animated;
            return this;
        }

        public Builder build() {
            return this;
        }

        public void invalidate() {
            draw = true;
            beforeTime = System.currentTimeMillis();
            initPaints();
            postInvalidate();
        }
    }
}
