package com.skysofttech.ratingview.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skysofttech.ratingview.R;
import com.skysofttech.ratingview.RatingUtils;
import com.skysofttech.ratingview.Utils;

import static com.skysofttech.ratingview.Utils.arraySum;
import static com.skysofttech.ratingview.Utils.fitTextSize;

/**
 * Author: Serhii Slobodianiuk
 * Date: 7/27/17
 */

public class MaterialRatingView extends LinearLayout {

    private static final String TAG = MaterialRatingView.class.getCanonicalName();

    private static final float DEFAULT_BAR_WIDTH = 0.65f;
    private static final int DEFAULT_BAR_CONTAINER_PADDING = R.dimen.barContainerPadding;
    private static final int DEFAULT_BAR_PADDING = R.dimen.barPadding;
    private static final int DEFAULT_TEXT_PADDING = R.dimen.textPadding;
    private static final int DEFAULT_ANIMATION_DURATION = 1500;

    @ColorInt
    private int barColor = R.color.colorAccent;

    @ColorInt
    private int numbersColor = android.R.color.black;

    @ColorInt
    private int totalCountColor = R.color.colorAccent;

    @DrawableRes
    private int drawableStar = android.R.drawable.star_on;

    private int[] countStars = {0, 0, 0, 0, 0};
    private boolean animated;

    @FloatRange(from = 0.0f, to = 1.0f)
    private float barWidth;

    @Px
    private int barContainerPadding;

    @Px
    private int barPadding;

    private Context context;

    private int[] totalRatingCount;
    private boolean draw = false;

    public MaterialRatingView(Context context) {
        super(context);
        this.context = context;
        setOrientation(HORIZONTAL);
    }

    public MaterialRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setupAttribute(attrs);
        setOrientation(HORIZONTAL);
    }

    public MaterialRatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setupAttribute(attrs);
        setOrientation(HORIZONTAL);
    }

    private void setupAttribute(AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.MaterialRatingView, 0, 0);

        barColor = attributes.getColor(R.styleable.MaterialRatingView_barColor,
                context.getResources().getColor(R.color.colorAccent));

        numbersColor = attributes.getColor(R.styleable.MaterialRatingView_numbersColor,
                context.getResources().getColor(android.R.color.black));

        totalCountColor = attributes.getColor(R.styleable.MaterialRatingView_totalCountColor,
                context.getResources().getColor(R.color.colorAccent));

        drawableStar = attributes.getInt(R.styleable.MaterialRatingView_drawableStar,
                android.R.drawable.star_on);

        barWidth = attributes.getFloat(R.styleable.MaterialRatingView_barWidth, DEFAULT_BAR_WIDTH);

        barContainerPadding = attributes.getDimensionPixelSize(R.styleable.MaterialRatingView_barContainerPadding,
                context.getResources().getDimensionPixelSize(DEFAULT_BAR_CONTAINER_PADDING));

        barPadding = attributes.getDimensionPixelSize(R.styleable.MaterialRatingView_barPadding,
                context.getResources().getDimensionPixelSize(DEFAULT_BAR_PADDING));

        attributes.recycle();
    }

    public Builder newBuilder() {
        return new Builder();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() != 0 && draw)
            drawView(animated);
    }

    private void drawView(boolean animated) {
        draw = false;
        int parentWidth = getMeasuredWidth();

        LinearLayout barsContainer = new LinearLayout(context);

        barsContainer.setLayoutParams(new LinearLayout.LayoutParams((int) (parentWidth * barWidth),
                ViewGroup.LayoutParams.WRAP_CONTENT));
        barsContainer.setOrientation(VERTICAL);
        barsContainer.setPadding(barContainerPadding, barContainerPadding,
                barContainerPadding, barContainerPadding);

        barsContainer.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);

        for (int i = countStars.length - 1; i >= 0; i--) {
            barsContainer.addView(drawRatingBar(i, barsContainer));
        }

        if (animated) {
            ResizeWidthAnimation animation = new ResizeWidthAnimation(barsContainer, 0, (int) (parentWidth * barWidth));
            animation.setDuration(DEFAULT_ANIMATION_DURATION);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    addView(drawTotalView());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            barsContainer.setAnimation(animation);
        }

        addView(barsContainer);
    }

    private LinearLayout drawRatingBar(final int index, final LinearLayout parent) {
        final LinearLayout barContainer = new LinearLayout(context);
        barContainer.setOrientation(HORIZONTAL);
        barContainer.setPadding(barPadding, barPadding, barPadding, barPadding);
        barContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        final TextView indexText = new TextView(context);
        indexText.setGravity(Gravity.CENTER);
        indexText.setPadding(barPadding, barPadding, barPadding, barPadding);
        indexText.setText(String.valueOf(index + 1));
        indexText.setTextColor(numbersColor);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        indexText.setLayoutParams(params);
        indexText.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        barContainer.addView(indexText);

        final ImageView imageStar = new ImageView(context);
        imageStar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageStar.setImageDrawable(context.getResources().getDrawable(drawableStar));
        imageStar.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        barContainer.addView(imageStar);

        parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (parent.getMeasuredWidth() <= 0 && parent.getMeasuredHeight() <= 0) {
                    return;
                }

                int maxBarLayoutWidth = parent.getMeasuredWidth() - barContainerPadding * 2 -
                        imageStar.getMeasuredWidth() - indexText.getMeasuredWidth() - barPadding * 2;
                int maxRatingValue = Utils.getMaxValue(countStars);

                RelativeLayout barLayout = new RelativeLayout(context);

                Drawable barDrawable = context.getResources().getDrawable(R.drawable.rounded_rectangle);
                barDrawable.setColorFilter(barColor, PorterDuff.Mode.SRC_IN);
                barLayout.setBackgroundDrawable(barDrawable);

                TextView count = new TextView(context);
                count.setGravity(Gravity.CENTER);
                count.setMaxLines(1);
                count.setTextColor(numbersColor);
                count.setText(String.valueOf(countStars[index]));
                count.setPadding(context.getResources().getDimensionPixelSize(DEFAULT_TEXT_PADDING), 0,
                        context.getResources().getDimensionPixelSize(DEFAULT_TEXT_PADDING), 0);
                count.setTextSize(fitTextSize(count.getPaint(), String.valueOf(countStars[index]), maxBarLayoutWidth, parent.getMeasuredHeight(), 2.5f));

                RelativeLayout.LayoutParams paramCount = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                paramCount.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                paramCount.addRule(RelativeLayout.CENTER_VERTICAL);
                count.setLayoutParams(paramCount);

                int barLayoutWidth = (countStars[index] * maxBarLayoutWidth) / maxRatingValue;
                int textPadding = context.getResources().getDimensionPixelOffset(DEFAULT_TEXT_PADDING);
                int minWidth = Utils.getMinTextSize(context, countStars[index], count.getTextSize())[1];

                barLayout.setLayoutParams(new RelativeLayout.LayoutParams(barLayoutWidth,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

                if (barLayoutWidth + minWidth + textPadding * 2 >= maxBarLayoutWidth) {
                    barLayout.addView(count);
                    barContainer.addView(barLayout);
                } else {
                    barContainer.setGravity(Gravity.CENTER_VERTICAL);
                    barContainer.addView(barLayout);
                    barContainer.addView(count);
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    parent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }

        });

        return barContainer;
    }

    private LinearLayout drawTotalView() {
        final LinearLayout totalsContainer = new LinearLayout(context);
        totalsContainer.setOrientation(VERTICAL);
        totalsContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        totalsContainer.setGravity(Gravity.CENTER);
        ((LayoutParams) totalsContainer.getLayoutParams()).gravity = Gravity.CENTER;

        totalsContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (totalsContainer.getMeasuredWidth() <= 0 && totalsContainer.getMeasuredHeight() <= 0) {
                    return;
                }

                TextView totalTextView = new TextView(context);
                totalTextView.setGravity(Gravity.CENTER);
                totalTextView.setTextColor(totalCountColor);

                double rating = Math.round(RatingUtils.getTotalRating(countStars) * 100.0) / 100.0;

                totalTextView.setText(String.valueOf(rating));
                totalTextView.setTextSize(fitTextSize(totalTextView.getPaint(), String.valueOf(RatingUtils.getTotalRating(countStars)),
                        totalsContainer.getMeasuredWidth(),
                        totalsContainer.getMeasuredHeight(), 1f));

                RatingBar bar = new RatingBar(context, null, android.R.attr.ratingBarStyleSmall);
                bar.setNumStars(countStars.length);
                bar.setStepSize(0.1f);
                bar.setRating((float) rating);

                LayerDrawable stars = (LayerDrawable) bar.getProgressDrawable();
                stars.getDrawable(2).setColorFilter(totalCountColor, PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.grey_hex_a0), PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.grey_hex_a0), PorterDuff.Mode.SRC_ATOP);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                bar.setLayoutParams(layoutParams);

                TextView totalReviewsView = new TextView(context);
                totalReviewsView.setGravity(Gravity.CENTER);
                totalReviewsView.setText(getResources().getString(R.string.reviews, String.valueOf(arraySum(countStars))));
                LayoutParams reviewsParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                reviewsParam.topMargin = 10;

                totalReviewsView.setLayoutParams(reviewsParam);

                totalsContainer.addView(totalTextView);
                totalsContainer.addView(bar);
                totalsContainer.addView(totalReviewsView);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    totalsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    totalsContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        return totalsContainer;
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

    public class Builder {

        private Builder() {
        }

        public void setBarColor(int barColor) {
            MaterialRatingView.this.barColor = barColor;
        }


        public Builder setNumbersColor(int numbersColor) {
            MaterialRatingView.this.numbersColor = numbersColor;
            return this;
        }


        public  Builder setTotalCountColor(int totalCountColor) {
            MaterialRatingView.this.totalCountColor = totalCountColor;
            return this;
        }

        public Builder setCountStars(int[] countStars) {
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

        public void build() {
            MaterialRatingView.this.draw = true;
            invalidate();
            requestLayout();
        }
    }
}
