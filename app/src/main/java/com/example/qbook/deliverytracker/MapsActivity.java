package com.example.qbook.deliverytracker;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

// , GoogleMap.OnInfoWindowClickListener
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    Context context;
    GetGPS GPSdata;//= new GetGPS(context);
    int i = 0;
    boolean wasPasswordEmpty;
    String login, id, ServerAddress;
    RequestQueue requestQueue;
    Localisation[] localisations;
    JSONArray localisationsJSON, deliveryPointsJSON;
    Location location;
    //MapsActivity map;
    SupportMapFragment mapFragment;
    PolylineOptions polylineOptions;
    ArrayList pointsArr, desc = null;
    Localisation currentLocalisation;
    ArrayList deliveryPoints;
    boolean ready=false;
    int dlugosc;
    Marker delivererMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localization_thread);


        Intent intent = getIntent();
        ServerAddress = intent.getSerializableExtra("ServerAddress").toString();

       /* //requestQueue = intent.getParcelableExtra("requestQueue");
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        login = intent.getSerializableExtra("login").toString();
        id = intent.getSerializableExtra("ID").toString();
        wasPasswordEmpty = intent.getBooleanExtra("wasPasswordEmpty", true);*/
        login = intent.getSerializableExtra("login").toString();
        id = intent.getSerializableExtra("ID").toString();
        wasPasswordEmpty = intent.getBooleanExtra("wasPasswordEmpty", true);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (wasPasswordEmpty) {
/*
            getLocalisations();
*/
            try {
                for (int i = 0; i < localisationsJSON.length(); i++) {
                    Log.d("Pozycja " + i + " JsonArray", localisationsJSON.getJSONObject(i).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            getDeliveryPoints();
            getLocalisations();
            GPSdata = new GetGPS(this, getID(), requestQueue,ServerAddress);
            //showOnMap();
            //GPSdata = new GetGPS(this, getID());
        }






        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                        GPSdata.getBestLocation();
                    getLocalisations();

                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {

                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            googleMap.clear();
                            deliveryPoints= new ArrayList<LatLng>();
                            double lat, lon;
                            int del;
                            lat = 0;
                            lon = 0;
                            int firstQueued = 0;

                            if (!wasPasswordEmpty) {
                                Marker tmpMarker;
                                for (int i = 0; i < deliveryPointsJSON.length(); i++) {
                                    try {
                                        lat = deliveryPointsJSON.getJSONObject(i).getDouble("Latitude");
                                        lon = deliveryPointsJSON.getJSONObject(i).getDouble("Longitude");
                                        del = deliveryPointsJSON.getJSONObject(i).getInt("Delivered");
                                        LatLng latlong = new LatLng(lat, lon);

                                        Log.i("deliveryPointJSON2", "latit: " + lat + "\nlongit: " + lon);
                                        if (del == 1) {
                                            googleMap.addMarker(new MarkerOptions()
                                                    .position(latlong)
                                                    .title("client " + i)
                                                    .snippet("" + GPSdata.getAddress(lat, lon))
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).setTag(del);
                                            if (i <= firstQueued)
                                                firstQueued += 1;
                                        } else {
                                            deliveryPoints.add(latlong);
                                            if (firstQueued == i) {
                                                tmpMarker = googleMap.addMarker(new MarkerOptions()
                                                        .position(latlong)
                                                        .title("client " + i)
                                                        .snippet("" + GPSdata.getAddress(lat, lon))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                                tmpMarker.setTag(del);
                                                tmpMarker.showInfoWindow();
                                            } else {
                                                tmpMarker = googleMap.addMarker(new MarkerOptions()
                                                        .position(latlong)
                                                        .title("client " + i)
                                                        .snippet("" + GPSdata.getAddress(lat, lon))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                                tmpMarker.setTag(del);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
//                                polylineOptions = new PolylineOptions();
//                                polylineOptions.color(Color.BLUE);
//                                polylineOptions.width(5);
//                                polylineOptions.addAll(deliveryPoints);
//                                googleMap.addPolyline(polylineOptions);
                            }

                            if (localisationsJSON != null) {

                                pointsArr = new ArrayList<LatLng>();
                                desc = new ArrayList<>();
                                long time = Calendar.getInstance().getTimeInMillis();
                                LatLng latLongTMP;
                                latLongTMP = new LatLng(0, 0);
                                String timeDeliverer = "now";
                                try {
                                    double tmp;
                                    for (int i = 0; i < localisationsJSON.length(); i++) {
                                        pointsArr.add(new LatLng(localisationsJSON.getJSONObject(i).getDouble("Latitude"), localisationsJSON.getJSONObject(i).getDouble("Longitude")));
                                        desc.add(localisationsJSON.getJSONObject(i).getInt("Time"));
                                        tmp=time / 1000 - localisationsJSON.getJSONObject(i).getLong("Time") / 1000;
                                        if (i == localisationsJSON.length() - 1) {
                                            latLongTMP = new LatLng(localisationsJSON.getJSONObject(i).getDouble("Latitude"), localisationsJSON.getJSONObject(i).getDouble("Longitude"));
                                            if (tmp>60) {
                                                if (tmp > 3600) {
                                                    timeDeliverer = "" + (int) (tmp / 3600) + "h" + (int) ((tmp % 3600) / 60) + "min" + (int)(tmp % 3600)%60+"s ago";
                                                } else
                                                    timeDeliverer = "" + (int) (tmp / 60) + "min" + (int)(tmp % 60)+"s ago";
                                            }else
                                                timeDeliverer = "" + (int)tmp + "s ago";
                                        }
                                    }
                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.BLUE);
                                    polylineOptions.width(5);
                                    polylineOptions.add(latLongTMP);
                                    polylineOptions.addAll(deliveryPoints);
                                    googleMap.addPolyline(polylineOptions);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

//                                polylineOptions = new PolylineOptions();
//                                polylineOptions.color(Color.BLUE);
//                                polylineOptions.width(5);
//                                polylineOptions.addAll(pointsArr);
//                                googleMap.addPolyline(polylineOptions);
                                try {
                                    delivererMarker.remove();
                                    delivererMarker=googleMap.addMarker(new MarkerOptions()
                                            .position(latLongTMP)
                                            .title("Deliverer's position")
                                            .snippet(timeDeliverer)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(localisationsJSON.getJSONObject(localisationsJSON.length() - 1).getDouble("Latitude"), localisationsJSON.getJSONObject(localisationsJSON.length() - 1).getDouble("Longitude")), 12));
                                } catch (Exception x) {
                                    x.printStackTrace();
                                }
                            } else
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));
                        }
                    });
                }
            
        });

    }

    Handler h = new Handler();
    int delay = 30 * 1000; //30 seconds
    Runnable runnable;



    public String getID() {
        return id;
    }

    public void getDeliveryPoints() {

        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, ServerAddress + "/route/" + id + "/deliveryPointsWithAlgorithm", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray array) { // odpowiedz z serwera
                try {   // informacje o użytkowniku
                    Log.d("Dlugosc JSONArray", "" + array.length());
                    dlugosc = array.length();
                    deliveryPointsJSON = array;

                    if(ready) {
                        showOnMap();
                    } else {
                        ready=true;
                    }
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
    }

    public void delivered(LatLng x, int a) {

        if (a == 0 || a == 1) {
            String address = ServerAddress + "/delivered";
            JSONObject object = new JSONObject();
            try { // pobranie parametrów do wysłania
                object.put("Longitude", x.longitude);
                object.put("Latitude", x.latitude);
                if (a == 0)
                    object.put("Delivered", false);
                else
                    object.put("Delivered", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, address, object,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        }
    }

    public void showOnMap() {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                deliveryPoints= new ArrayList<LatLng>();
                double lat, lon;
                int del;
                lat = 0;
                lon = 0;
                int firstQueued = 0;

                if (!wasPasswordEmpty) {
                    Marker tmpMarker;
                    for (int i = 0; i < deliveryPointsJSON.length(); i++) {
                        try {
                            lat = deliveryPointsJSON.getJSONObject(i).getDouble("Latitude");
                            lon = deliveryPointsJSON.getJSONObject(i).getDouble("Longitude");
                            del = deliveryPointsJSON.getJSONObject(i).getInt("Delivered");
                            LatLng latlong = new LatLng(lat, lon);

                            Log.i("deliveryPointJSON2", "latit: " + lat + "\nlongit: " + lon);
                            if (del == 1) {
                                googleMap.addMarker(new MarkerOptions()
                                        .position(latlong)
                                        .title("client " + i)
                                        .snippet("" + GPSdata.getAddress(lat, lon))
                                        //.title("klient"+ i)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).setTag(del);
                                if (i <= firstQueued)
                                    firstQueued += 1;
                            } else {
                                deliveryPoints.add(latlong);
                                if (firstQueued == i) {
                                    tmpMarker = googleMap.addMarker(new MarkerOptions()
                                            .position(latlong)
                                            .title("client " + i)
                                            .snippet("" + GPSdata.getAddress(lat, lon))
                                            //.title("klient"+ i)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                    tmpMarker.setTag(del);
                                    tmpMarker.showInfoWindow();
                                } else {
                                    tmpMarker = googleMap.addMarker(new MarkerOptions()
                                            .position(latlong)
                                            .title("client " + i)
                                            .snippet("" + GPSdata.getAddress(lat, lon))
                                            //.title("klient"+ i)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                    tmpMarker.setTag(del);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                if (localisationsJSON != null) {

                    pointsArr = new ArrayList<LatLng>();
                    desc = new ArrayList<>();
                    long time = Calendar.getInstance().getTimeInMillis();
                    LatLng latLongTMP;
                    latLongTMP = new LatLng(0, 0);
                    String timeDeliverer = "now";
                    try {
                        double tmp;
                        for (int i = 0; i < localisationsJSON.length(); i++) {
                            pointsArr.add(new LatLng(localisationsJSON.getJSONObject(i).getDouble("Latitude"), localisationsJSON.getJSONObject(i).getDouble("Longitude")));
                            desc.add(localisationsJSON.getJSONObject(i).getInt("Time"));
                            tmp=time / 1000 - localisationsJSON.getJSONObject(i).getLong("Time") / 1000;
                            if (i == localisationsJSON.length() - 1) {
                                latLongTMP = new LatLng(localisationsJSON.getJSONObject(i).getDouble("Latitude"), localisationsJSON.getJSONObject(i).getDouble("Longitude"));
                                if (tmp>60) {
                                    if (tmp > 3600) {
                                        timeDeliverer = "" + (int) (tmp / 3600) + "h" + (int) ((tmp % 3600) / 60) + "min" + (int)(tmp % 3600)%60+"s ago";
                                    } else
                                        timeDeliverer = "" + (int) (tmp / 60) + "min" + (int)(tmp % 60)+"s ago";
                                }else
                                    timeDeliverer = "" + (int)tmp + "s ago";
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//
                    try {
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLongTMP)
                                .title("Deliverer's position")
                                .snippet(timeDeliverer)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.BLUE);
                        polylineOptions.width(5);
                        polylineOptions.add(latLongTMP);
                        polylineOptions.addAll(deliveryPoints);
                        googleMap.addPolyline(polylineOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(localisationsJSON.getJSONObject(localisationsJSON.length() - 1).getDouble("Latitude"), localisationsJSON.getJSONObject(localisationsJSON.length() - 1).getDouble("Longitude")), 12));
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                } else
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));
            }
        });

    }


    public void getLocalisations() {
        String showUrl = ServerAddress + "/route/" + id;
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, showUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray array) { // odpowiedz z serwera
                try {   // informacje o użytkowniku
                    Log.d("Dlugosc JSONArray", "" + array.length());
                    dlugosc = array.length();
                    localisationsJSON = array;
                    if(ready) {
                        showOnMap();
                    } else {
                        ready=true;
                    }
                   // Tast.makeText(getApplicationContext(), array.getJSONObject(i).toString(), Toast.LENGTH_SHORT);

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            //wait(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }


            @Override
            public View getInfoContents(Marker marker) {
                String isDelivered;

                if (marker.getTag() instanceof Integer && (Integer) marker.getTag() == 0) {
                    isDelivered = "\nWhen delivery will be finished tap and hold this notification";
                } else if(marker.getTag() instanceof Integer && (Integer) marker.getTag() == 1){
                    isDelivered = "\nDelivered";
                }
                else isDelivered = "";
                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.


                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet() + isDelivered);
                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        double lat, lon;
        boolean del;
        lat = 0;
        lon = 0;
        Marker tmpMarker;
        if (deliveryPointsJSON != null) {
            for (int i = 0; i < deliveryPointsJSON.length(); i++) {
                try {
                    lat = deliveryPointsJSON.getJSONObject(i).getDouble("Latitude");
                    lon = deliveryPointsJSON.getJSONObject(i).getDouble("longitude");
                    del = deliveryPointsJSON.getJSONObject(i).getBoolean("Delivered");
                    LatLng latlong = new LatLng(lat, lon);
                    Log.d("deliveryPointJSON", "latit: " + lat + "\nlongit: " + lon);
                    int firstQueued = 0;
                    if (!wasPasswordEmpty) {
                        if (del) {
                            tmpMarker = googleMap.addMarker(new MarkerOptions()
                                    .position(latlong)
                                    .title("client " + i)
                                    .snippet("" + GPSdata.getAddress(lat, lon))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            tmpMarker.setTag(del);
                            if (i <= firstQueued)
                                firstQueued += 1;
                        } else {
                            if (firstQueued == i) {
                                tmpMarker = googleMap.addMarker(new MarkerOptions()
                                        .position(latlong)
                                        .title("client " + i)
                                        .snippet("" + GPSdata.getAddress(lat, lon))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                tmpMarker.setTag(del);
                                tmpMarker.showInfoWindow();

                            } else {
                                tmpMarker = googleMap.addMarker(new MarkerOptions()
                                        .position(latlong)
                                        .title("client " + i)
                                        .snippet("" + GPSdata.getAddress(lat, lon))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                tmpMarker.setTag(del);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            // googleMap.setOnInfoWindowClickListener(this);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));
        }

        if (localisationsJSON != null) {

            pointsArr = new ArrayList<>();
            desc = new ArrayList<>();
            long time = Calendar.getInstance().getTimeInMillis();
            LatLng latLongTMP;
            latLongTMP = new LatLng(0, 0);
            String timeDeliverer = "now";

            try{
                double tmp;
                tmp=time / 1000 - localisationsJSON.getJSONObject(i).getLong("Time") / 1000;
                if (i == localisationsJSON.length() - 1) {
                    latLongTMP = new LatLng(localisationsJSON.getJSONObject(i).getDouble("Latitude"), localisationsJSON.getJSONObject(i).getDouble("Longitude"));
                    if (tmp>60) {
                        if (tmp > 3600) {
                            timeDeliverer = "" + (int) (tmp / 3600) + "h" + (int) ((tmp % 3600) / 60) + "min" + (int)(tmp % 3600)%60+"s ago";
                        } else
                            timeDeliverer = "" + (int) (tmp / 60) + "min" + (int)(tmp % 60)+"s ago";
                    }else
                        timeDeliverer = "" + (int)tmp + "s ago";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.BLUE);
            polylineOptions.width(5);
            polylineOptions.addAll(pointsArr);
            googleMap.addPolyline(polylineOptions);
            try {
                googleMap.addMarker(new MarkerOptions()
                        .position(latLongTMP)
                        //
                        // .title(GPSdata.getAddress(latlong))
                        .title("Deliverer's position")
                        .snippet(timeDeliverer)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(localisationsJSON.getJSONObject(localisationsJSON.length()-1).getDouble("Latitude"), localisationsJSON.getJSONObject(localisationsJSON.length()-1).getDouble("Longitude")), 15));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        googleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                String isDelivered = "";
                try {
                    if (marker.getTag() instanceof Integer && (Integer) marker.getTag() == 0) {
                        delivered(marker.getPosition(), 1);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        marker.setTag(1);
                    } else if (marker.getTag() instanceof Integer && (Integer) marker.getTag() == 1) {
                        delivered(marker.getPosition(), 0);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        marker.setTag(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!wasPasswordEmpty)
            GPSdata.stopUsingGPS();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!wasPasswordEmpty)
            GPSdata.stopUsingGPS();
        h.removeCallbacks(runnable); //stop handler when activity not visible
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!wasPasswordEmpty)
            GPSdata.init();
        h.postDelayed(new Runnable() {
            public void run() {
                JSONObject employeeIdForLocationRequestjsonObject = new JSONObject();
                try {
                    employeeIdForLocationRequestjsonObject.put("employeeID",id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                runnable = this;

                h.postDelayed(runnable, delay);
            }
        }, delay);
    }

}