package com.skysofttech.ratingview;

import android.app.Application;

import com.thefinestartist.Base;

/**
 * Author: Serhii Slobodianiuk
 * Date: 7/31/17
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Base.initialize(this);
    }
}
