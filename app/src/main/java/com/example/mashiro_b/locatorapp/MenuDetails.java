package com.example.mashiro_b.locatorapp;

/**
 * Created by Mashiro_B on 2017/12/12.
 */

import android.support.v7.app.AppCompatActivity;

public class MenuDetails {

    public final int titleId;
    public final int descriptionId;
    public final Class<? extends AppCompatActivity> activityClass;

    public MenuDetails(int titleId, int descriptionId, Class<? extends AppCompatActivity> activityClass) {
        this.titleId = titleId;
        this.descriptionId = descriptionId;
        this.activityClass = activityClass;
    }
}
