package com.example.mashiro_b.locatorapp;

/**
 * Created by Mashiro_B on 2017/12/12.
 */

public final class MenuDetailsList {

    private MenuDetailsList() {
    }

    public static final MenuDetails[] MENUS = {
            new MenuDetails(R.string.start_locator_label, R.string.start_locator_description, LocatorActivity.class),
            new MenuDetails(R.string.setting_label, R.string.setting_description, SettingsActivity.class),
    };

}
