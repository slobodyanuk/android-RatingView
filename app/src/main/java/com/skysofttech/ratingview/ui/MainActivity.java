package com.skysofttech.ratingview.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.skysofttech.ratingview.widgets.MaterialRatingView;
import com.skysofttech.ratingview.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialRatingView ratingView = findViewById(R.id.ratingView);

        ratingView.newBuilder()
                .setCountStars(new long[]{99999, 24141, 7480, 400, 5484})
                .setAnimated(true)
                .invalidate();
    }
}
