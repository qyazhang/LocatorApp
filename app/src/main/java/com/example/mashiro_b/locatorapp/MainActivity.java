package com.example.mashiro_b.locatorapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setEmptyView(findViewById(R.id.empty));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        MenuDetails menu = (MenuDetails) parent.getAdapter().getItem(position);
        startActivity(new Intent(this, menu.activityClass));
    }

}
