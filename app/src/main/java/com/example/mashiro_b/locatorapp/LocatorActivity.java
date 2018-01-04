package com.example.mashiro_b.locatorapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;

/**
 * This shows how to change the camera position for the map.
 */
public class LocatorActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final String TAG = LocatorActivity.class.getName();

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted ;
    private final LatLng mDefaultLocation = new LatLng(35.605130, 139.683446);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final List<Marker> markers = new ArrayList<Marker>();

    private Location mLastKnownLocation;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private ToggleButton start_locator;
    private static boolean CAN_SET_MAKER_RECORD_LOCATION = false;
    private static String SAVE_FILENAME;
    public static final String mComma = ",";
    private static StringBuilder mStringBuilder = null;
    private static String mFileName = null;
    private static Boolean HAVE_DATA_SAVED = false;
    private static Boolean mIsChecked = false;

    private static String myID = "A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.locator);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        start_locator = (ToggleButton) findViewById(R.id.start_locator);
        start_locator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    mIsChecked = true;
                    CAN_SET_MAKER_RECORD_LOCATION = true;
                    SAVE_FILENAME = setTime();
                    mStringBuilder = new StringBuilder();
                    initCSVSaver();
                    HAVE_DATA_SAVED = false;
                    System.out.println(SAVE_FILENAME);
                } else {
                    // The toggle is disabled
                    mIsChecked = false;
                    CAN_SET_MAKER_RECORD_LOCATION = false;
                    if (HAVE_DATA_SAVED&&mStringBuilder.indexOf("\n")!=-1) {
                        System.out.println("Before flush");
                        quitSaveDialog(0);
                    }
                    HAVE_DATA_SAVED = false;
                }
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.select_my_ID);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                String[] my_ID = getResources().getStringArray(R.array.my_ID);
                Toast.makeText(LocatorActivity.this, "Your ID is :"+my_ID[pos], Toast.LENGTH_SHORT).show();
                myID = my_ID[pos];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK&&mIsChecked&&HAVE_DATA_SAVED&&mStringBuilder.indexOf("\n")!=-1){
            quitSaveDialog(1);
        }
        else finish();
        mIsChecked = false;
        CAN_SET_MAKER_RECORD_LOCATION = false;
        HAVE_DATA_SAVED = false;
        return false;
    }

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;
        getLocationPermission();
        //System.out.println("Before update");
        updateLocationUI();
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            System.out.println(mLastKnownLocation.getLatitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //System.out.println("Given Permission");
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            //System.out.println("Not Given Permission");
        }
        //I don't why it directly go to else branch even if I choose I give it the permission for location,
        //so I directly give the mLocationPermissionGranted true
        //System.out.println(mLocationPermissionGranted);
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        sleep(500);
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        //System.out.println("In update");
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                sleep(500);
                getLocationPermission();
                //System.out.println("In false Update UI");
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    private void createMaker (final char ID) {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()))
                                    .icon(BitmapDescriptorFactory.defaultMarker(360/12*(ID-'A'))));
                            markers.add(marker);

                            writeCSVOneline(setTime(), myID, String.valueOf(ID),
                                    String.valueOf(mLastKnownLocation.getLatitude()),
                                    String.valueOf(mLastKnownLocation.getLongitude()));
                            HAVE_DATA_SAVED = true;
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void onChooseA (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('A');
        }
    }

    public void onChooseB (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('B');
        }
    }

    public void onChooseC (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('C');
        }
    }

    public void onChooseD (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('D');
        }
    }

    public void onChooseE (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('E');
        }
    }

    public void onChooseF (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('F');
        }
    }

    public void onChooseG (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('G');
        }
    }

    public void onChooseH (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('H');
        }
    }

    public void onChooseI (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('I');
        }
    }

    public void onChooseJ (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('J');
        }
    }

    public void onChooseK (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('K');
        }
    }

    public void onChooseL (View view) {
        if (CAN_SET_MAKER_RECORD_LOCATION) {
            createMaker('L');
        }
    }

    private String setTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    private static void initCSVSaver() {
        String folderName = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (path != null) {
                System.out.println("in make folder name");
                folderName = path +"/CSV/";
            }
        }
        System.out.println(folderName);
        File fileRobo = new File(folderName);
        if(!fileRobo.exists()){
            Boolean err = fileRobo.mkdir();
            System.out.println(err);
            System.out.println(fileRobo.toString());
        }
        mFileName = folderName + SAVE_FILENAME +".csv";
        mStringBuilder = new StringBuilder();
        mStringBuilder.append("date");
        mStringBuilder.append(mComma);
        mStringBuilder.append("myID");
        mStringBuilder.append(mComma);
        mStringBuilder.append("otherID");
        mStringBuilder.append(mComma);
        mStringBuilder.append("Latitude");
        mStringBuilder.append(mComma);
        mStringBuilder.append("Longtitude");

    }

    private static void writeCSVOneline(String value1, String value2, String value3, String value4, String value5) {
        mStringBuilder.append("\n");
        mStringBuilder.append(value1);
        mStringBuilder.append(mComma);
        mStringBuilder.append(value2);
        mStringBuilder.append(mComma);
        mStringBuilder.append(value3);
        mStringBuilder.append(mComma);
        mStringBuilder.append(value4);
        mStringBuilder.append(value5);
        mStringBuilder.append(mComma);
    }

    private static void flush() {
        if (mFileName != null) {
            try {
                File file = new File(mFileName);
                FileOutputStream fos = new FileOutputStream(file, true);
                System.out.println(mStringBuilder.toString());
                fos.write(mStringBuilder.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("You should call initCSVSaver before flush()");
        }
    }

    public void onCancelLastStep (View view) {
        if (mStringBuilder==null||mStringBuilder.indexOf("\n")==-1) {
            Toast.makeText(LocatorActivity.this, "There is no data and maker to cancel", Toast.LENGTH_SHORT).show();
        }
        else {
            mStringBuilder.delete(mStringBuilder.lastIndexOf("\n"), mStringBuilder.length());
            System.out.println(mStringBuilder.toString()+"aaa");
            Marker temp = markers.get(markers.size()-1);
            temp.remove();
            markers.remove(markers.size()-1);
        }
    }

    protected void quitSaveDialog(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to SAVE?");
        builder.setTitle("SAVE");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                flush();
                Toast.makeText(LocatorActivity.this, "The data has been saved", Toast.LENGTH_SHORT).show();
                if (type==1) finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (type==1) finish();
            }
        });
        builder.create().show();
    }

}