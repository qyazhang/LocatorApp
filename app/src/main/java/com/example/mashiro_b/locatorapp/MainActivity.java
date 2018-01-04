package com.example.mashiro_b.locatorapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public final class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private static final String SERVER_ADDRESS = "192.168.0.1";

    private static class CustomArrayAdapter extends ArrayAdapter<MenuDetails>{

        public CustomArrayAdapter(Context context, MenuDetails[] menu) {
            super (context, R.layout.feature, R.id.title, menu);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FeatureView featureView;
            if (convertView instanceof FeatureView) {
                featureView = (FeatureView) convertView;
            } else {
                featureView = new FeatureView(getContext());
            }

            MenuDetails menu = getItem(position);

            featureView.setTitleId(menu.titleId);
            featureView.setDescriptionId(menu.descriptionId);

            Resources resources = getContext().getResources();
            String title = resources.getString(menu.titleId);
            String description = resources.getString(menu.descriptionId);
            featureView.setContentDescription(title + ". " + description);

            return featureView;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = (ListView) findViewById(R.id.list);

        ListAdapter adapter = new CustomArrayAdapter(this, MenuDetailsList.MENUS);

        verifyStoragePermissions(this);

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setEmptyView(findViewById(R.id.empty));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        MenuDetails menu = (MenuDetails) parent.getAdapter().getItem(position);
        startActivity(new Intent(this, menu.activityClass));
    }

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
