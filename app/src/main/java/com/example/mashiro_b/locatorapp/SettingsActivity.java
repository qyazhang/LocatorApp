package com.example.mashiro_b.locatorapp;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getName();

    private static String uploadUrl = "http://133.18.171.238:3000/upload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void onDeleteLocalData(View view) {
        deleteDialog();
    }

    public void onUploadData(View view) throws IOException {
        uploadAllData();
    }

    protected void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to DELETE?");
        builder.setTitle("DELETE");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String filePath = tempPath+"/CSV/";
                File fileRobo = new File(filePath);
                if(fileRobo.exists()){
                    RecursionDeleteFile(fileRobo);
                    Toast.makeText(SettingsActivity.this, "Successfully delete the data", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SettingsActivity.this, "No data to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

    private String parseFile(File file) throws IOException {
        StringBuffer sb= new StringBuffer("");

        FileReader reader = new FileReader(file.getPath());

        BufferedReader br = new BufferedReader(reader);

        String str = null;

        while((str = br.readLine()) != null) {

            sb.append(str+"/n");

        }
        return sb.toString();
    }

    private void upLoadByPost(File path) throws IOException {

        RequestParams params = new RequestParams();

        params.put("location_data", path);
        System.out.println(params);

        AsyncHttpClient client = new AsyncHttpClient();

        client.post(uploadUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Toast.makeText(SettingsActivity.this, "Upload Success!", Toast.LENGTH_LONG).show();

            }
            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(SettingsActivity.this, "Upload Fail!", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void uploadAllData() throws IOException {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folderName = path +"/CSV/";
        File dataFile = new File(folderName);
        File[] allData = dataFile.listFiles();
        for (int i=0;i<allData.length;i++) {
            upLoadByPost( allData[i] );
        }
    }

}
