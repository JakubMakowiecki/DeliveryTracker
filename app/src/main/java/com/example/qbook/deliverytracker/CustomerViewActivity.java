package com.example.qbook.deliverytracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomerViewActivity extends FragmentActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationClient;

    private GoogleMap mMap;
    private int lat=-34;
    private int lng=151;
    private LatLng clientLocation;
    private String employeeId;
    private LatLng deliverer;
    private String ServerAddress;
    private String insertUrl;
    private RequestQueue requestQueue;
    private int delivererID;
    JSONArray localisationsJSON;
    private int dlugosc, i =0;
    ArrayList pointsArr, desc = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Intent intent = getIntent();
        delivererID=intent.getIntExtra("delivererID",1);
        ServerAddress=intent.getSerializableExtra("ServerAddress").toString();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        insertUrl=ServerAddress+""; //TODO add url
        requestQueue = Volley.newRequestQueue(getApplicationContext());



    }

    Handler h = new Handler();
    int delay = 5 * 1000; //5 seconds
    Runnable runnable;

    @Override
    protected void onResume() {
        //start handler as activity become visible

        h.postDelayed(new Runnable() {
            public void run() {
                JSONObject employeeIdForLocationRequestjsonObject = new JSONObject();
                try {
                    employeeIdForLocationRequestjsonObject.put("employeeID",delivererID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String showUrl = ServerAddress + "/route/" + delivererID;
                final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, showUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray array) { // odpowiedz z serwera
                        try {   // informacje o użytkowniku
                            Log.d("Dlugosc JSONArray", "" + array.length());
                            dlugosc = array.length();
                            localisationsJSON = array;
                            pointsArr = new ArrayList<LatLng>();
                            desc = new ArrayList<Integer>();
                            LatLng latLongTMP= new LatLng(0,0);
                            long time = Calendar.getInstance().getTimeInMillis();
                            String timeDeliverer = "now";
                            double tmp;
                            Log.d("log:", array.toString());
                            for (int i = 0; i < array.length(); i++) {
                                    pointsArr.add(new LatLng(array.getJSONObject(i).getDouble("Latitude"), array.getJSONObject(i).getDouble("Longitude")));
                                    desc.add(array.getJSONObject(i).getInt("Time"));
                                    tmp=time / 1000 - array.getJSONObject(i).getLong("Time") / 1000;
                                    if (i == array.length() - 1) {
                                        latLongTMP = new LatLng(array.getJSONObject(i).getDouble("Latitude"), array.getJSONObject(i).getDouble("Longitude"));
                                        if (tmp>60) {
                                            if (tmp > 3600) {
                                                timeDeliverer = "" + (int) (tmp / 3600) + "h" + (int) ((tmp % 3600) / 60) + "min" + (int)(tmp % 3600)%60+"s ago";
                                            } else
                                                timeDeliverer = "" + (int) (tmp / 60) + "min" + (int)(tmp % 60)+"s ago";
                                        }else
                                            timeDeliverer = "" + (int)tmp + "s ago";
                                    }
                                }

                            deliverer = latLongTMP;
                            mMap.clear();
                            mMap.addCircle(new CircleOptions().center(deliverer).radius(100));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliverer,15));

                            Toast.makeText(getApplicationContext(), array.getJSONObject(i).toString(), Toast.LENGTH_SHORT);

                        } catch (Exception e1) {
                            e1.printStackTrace();
                            Toast.makeText(getApplicationContext(), e1.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        error.getMessage();
                        error.toString();
                    }
                });
                requestQueue.add(jsonArrayRequest);


//
                runnable = this;

                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        h.removeCallbacks(runnable); //stop handler when activity not visible
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        h.removeCallbacks(runnable); //stop handler when activity not visible

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            clientLocation= new LatLng(location.getLatitude(),location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clientLocation,15));
                        }
                    }
                });

    }
}
