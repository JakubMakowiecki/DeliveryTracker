package com.example.qbook.deliverytracker;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EmployerViewActivity extends FragmentActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationClient;

    private GoogleMap mMap;
    private int lat=-34;
    private int lng=151;
    private LatLng clientLocation;
    private String employeeId;
    private LatLng deliverer;
    private String serverAddress;
    private String insertUrl;
    private RequestQueue requestQueue;
    private int delivererID;
    JSONArray localisationsJSON;
    private int dlugosc, i =0;
    ArrayList pointsArr, desc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_view);



        Intent intent = getIntent();
        //delivererID=Integer.getInteger(intent.getSerializableExtra("delivererID").toString());
        serverAddress = intent.getSerializableExtra("ServerAddress").toString();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //TODO send employee id with intent
        //employeeId= intent.toString()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        insertUrl=serverAddress+""; //TODO add url
        requestQueue = Volley.newRequestQueue(getApplicationContext());



    }

    Handler h = new Handler();
    int delay = 10 * 1000; //10 seconds
    Runnable runnable;

    @Override
    protected void onResume() {
        //start handler as activity become visible
        h.postDelayed(new Runnable() {
            public void run() {
                mMap.clear();

                sendCall(2);

                //sendCall(3);
                //sendCall(4);
                //sendCall(5);

                runnable = this;

                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    public void sendCall(final int userId) {
        String showUrl = serverAddress + "/route/" + userId;
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, showUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray array) { // odpowiedz z serwera
                try {   // informacje o użytkowniku
                    Log.d("Dlugosc JSONArray", "" + array.length());
                    int dlug = array.length();
                    JSONArray local = array;
                    ArrayList<LatLng> pointArr = new ArrayList<>();
                    ArrayList descending = new ArrayList<Integer>();
                    LatLng latLongTMP = new LatLng(0, 0);
                    long time = Calendar.getInstance().getTimeInMillis();
                    String timeDeliverer = "now";
                    double tmp;
                    Log.d("log:", array.toString());
                    for (int i = 0; i < array.length(); i++) {
                        pointArr.add(new LatLng(array.getJSONObject(i).getDouble("Latitude"), array.getJSONObject(i).getDouble("Longitude")));
                        descending.add(array.getJSONObject(i).getInt("Time"));
                        tmp = time / 1000 - array.getJSONObject(i).getLong("Time") / 1000;
                        if (i == array.length() - 1) {
                            latLongTMP = new LatLng(array.getJSONObject(i).getDouble("Latitude"), array.getJSONObject(i).getDouble("Longitude"));
                            if (tmp > 60) {
                                if (tmp > 3600) {
                                    timeDeliverer = "" + (int) (tmp / 3600) + "h" + (int) ((tmp % 3600) / 60) + "min" + (int) (tmp % 3600) % 60 + "s ago";
                                } else
                                    timeDeliverer = "" + (int) (tmp / 60) + "min" + (int) (tmp % 60) + "s ago";
                            } else
                                timeDeliverer = "" + (int) tmp + "s ago";
                        }
                    }

                    final LatLng deliv = latLongTMP;
                    mMap.addMarker(new MarkerOptions()
                            .position(deliv)
                            .title("deliverer " + userId) //TODO add name
                            //.snippet("" + getAddress(deliverer.latitude, deliverer.longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    //               mMap.addCircle(new CircleOptions().center(deliv).radius(5));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliv, 12));

                    //Toast.makeText(getApplicationContext(), array.getJSONObject(i).toString(), Toast.LENGTH_SHORT);

                } catch (Exception e1) {
                    e1.printStackTrace();
                    Toast.makeText(getApplicationContext(), e1.toString(), Toast.LENGTH_LONG).show();
                }
sendCall2();
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

    public void sendCall2() {
        final int userId=3;
        String showUrl = serverAddress + "/route/" + userId;
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, showUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray array) { // odpowiedz z serwera
                try {   // informacje o użytkowniku
                    Log.d("Dlugosc JSONArray", "" + array.length());
                    int dlug = array.length();
                    JSONArray local = array;
                    ArrayList<LatLng> pointArr = new ArrayList<>();
                    ArrayList descending = new ArrayList<Integer>();
                    LatLng latLongTMP = new LatLng(0, 0);
                    long time = Calendar.getInstance().getTimeInMillis();
                    String timeDeliverer = "now";
                    double tmp;
                    Log.d("log:", array.toString());
                    for (int i = 0; i < array.length(); i++) {
                        pointArr.add(new LatLng(array.getJSONObject(i).getDouble("Latitude"), array.getJSONObject(i).getDouble("Longitude")));
                        descending.add(array.getJSONObject(i).getInt("Time"));
                        tmp = time / 1000 - array.getJSONObject(i).getLong("Time") / 1000;
                        if (i == array.length() - 1) {
                            latLongTMP = new LatLng(array.getJSONObject(i).getDouble("Latitude"), array.getJSONObject(i).getDouble("Longitude"));
                            if (tmp > 60) {
                                if (tmp > 3600) {
                                    timeDeliverer = "" + (int) (tmp / 3600) + "h" + (int) ((tmp % 3600) / 60) + "min" + (int) (tmp % 3600) % 60 + "s ago";
                                } else
                                    timeDeliverer = "" + (int) (tmp / 60) + "min" + (int) (tmp % 60) + "s ago";
                            } else
                                timeDeliverer = "" + (int) tmp + "s ago";
                        }
                    }

                    final LatLng deliv = latLongTMP;
                    mMap.addMarker(new MarkerOptions()
                            .position(deliv)
                            .title("deliverer " + userId) //TODO add name
                            //.snippet("" + getAddress(deliverer.latitude, deliverer.longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                  //mMap.addCircle(new CircleOptions().center(deliv).radius(5));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliv, 12));

                    //Toast.makeText(getApplicationContext(), array.getJSONObject(i).toString(), Toast.LENGTH_SHORT);

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
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    public String getAddress(double latit,double longit) {
        String errorMessage=null;
        String cAddress = "";
        if (latit == 0 && longit == 0) {
            errorMessage = "no_location_data_provided";
            Log.wtf("TAG", errorMessage);
            return "";
        }
        Geocoder geocoder = new Geocoder(this);//, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    //latLng.latitude,
                    //latLng.longitude,
                    latit,longit,
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service_not_available";
            Log.e("TAG", errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid_lat_long_used";
            Log.e("TAG", errorMessage + ". " +
                    "Latitude = " + latit +
                    ", Longitude = " + longit, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no_address_found";
                Log.e("TAG", errorMessage);
            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            String allAddress = "";
//            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                addressFragments.add(address.getAddressLine(i));
//                allAddress += address.getAddressLine(i) + " ";
//            }
            String city, postalCode,addr;

            if (address.getLocality() != null) {
                city = address.getLocality();
            } else {
                city = "";
            }
            if(address.getAddressLine(0)!=null) {
                addr = address.getAddressLine(0);
            } else {
                addr = "";
            }
            if (address.getPostalCode() != null) {
                postalCode = address.getPostalCode();
            } else {
                postalCode = "";
            }

            //Log.i("TAG", "address_found");
            //driverAddress = TextUtils.join(System.getProperty("line.separator"), addressFragments);
            cAddress = "\nPostal Code: " + postalCode + "\nCity: " + city + "\nAddress: "+ addr;
            //Log.e("result", cAddress.toString());
        }
        return cAddress;
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

        sendCall(2);
        //sendCall(3);
        //sendCall(4);
        //sendCall(5);
    }
}
