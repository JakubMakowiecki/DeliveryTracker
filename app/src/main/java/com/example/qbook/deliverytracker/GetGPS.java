package com.example.qbook.deliverytracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class GetGPS extends Service implements LocationListener {
    private Context mContext;
    LocationManager locationManager = null;
    String id;
    RequestQueue requestQueue;
    String serverAddress;

    Location BestLocationNow = null;
    Location temp;
    Location GPSlocation = null;
    Location Netlocation = null;
    static final int CheckTime = 10000;
    static final int CheckDistance = 100;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    public GetGPS(Context context, String id, RequestQueue rq, String serverAddress) {
        this.mContext = context;
        this.id = id;
        requestQueue = rq;
        this.serverAddress=serverAddress;
        init();
        onLocationChanged(getBestLocation());
    }

    public GetGPS(Context context, String id) {
        this.mContext = context;
        this.id = id;
        init();
        onLocationChanged(getBestLocation());
    }
    public void init() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showSettingsAlert();
            showSettingsAlertInternet();
        }
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getBestLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showSettingsAlert();
            showSettingsAlertInternet();
        } else {
            this.canGetLocation = true;
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CheckTime, CheckDistance, this);
                Log.d("!!!!Network is Enabled", "Network is Enabled");
                if (locationManager != null) {
                    Netlocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Netlocation.setTime(Calendar.getInstance().getTimeInMillis());
                }
            }
            if (isGPSEnabled)
                if (GPSlocation == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CheckTime, CheckDistance, this);
                    Log.d("!!!!GPS is ENABLED", " GPS is ENABLED");
                    if (locationManager != null) {
                        GPSlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(GPSlocation!=null)
                            GPSlocation.setTime(Calendar.getInstance().getTimeInMillis());


                    }
                }
        }


        return whichLocationBetter();
    }


    public Location whichLocationBetter() {
        if (this.GPSlocation == null)
            if (this.Netlocation == null) {
                Log.d("1", "whichLocationBetter1");
                return null;
            } else {

                Log.d("1", "whichLocationBetter2");
                return Netlocation;
            }
        else {

            if (this.Netlocation == null) {
                Log.d("1", "1");
                return GPSlocation;
            }
            if (this.GPSlocation.getAccuracy() < this.Netlocation.getAccuracy()) {
                if (this.Netlocation.getAccuracy() > this.GPSlocation.getAccuracy() * 3) {
                    Log.d("1", "2");
                    return GPSlocation;
                }
                if (this.GPSlocation.getTime() + CheckTime > this.Netlocation.getTime()) {
                    Log.d("1", "3");
                    return GPSlocation;
                } else {
                    Log.d("1", "4");
                    return Netlocation;
                }
            } else if (this.GPSlocation.getAccuracy() > this.Netlocation.getAccuracy() * 3) {
                Log.d("1", "5");
                return Netlocation;
            }
            if (GPSlocation.getTime() < Netlocation.getTime() + CheckTime) {
                Log.d("1", "6");
                return Netlocation;
            } else {
                Log.d("1", "7");
                return GPSlocation;
            }
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != BestLocationNow) {



            Log.d("!!!!!!Nowa lokalizacja " + location.getProvider(), "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude() + "\nTime: " + location.getTime() + "\nAcc: " + location.getAccuracy());

            Toast.makeText(mContext,location.getProvider()+ "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude() + "\nTime: " + location.getTime() + "\nAcc: " + location.getAccuracy() , Toast.LENGTH_LONG).show();


            if (setNewLocation(location)) {
                Log.d("PROVIDER!!!!!!!!!!!!!! ", "przeszlo setNewLocation");

                //Http Post Method
                JSONObject object = new JSONObject();
                try { // pobranie parametrów do wysłania
                    object.put("Time", getTime());
                    object.put("Accuracy", getAccuracy());
                    object.put("Longitude", getLongitude());
                    object.put("Latitude", getLatitude());
                    object.put("user_id",id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, serverAddress + "/route", object,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("Wysyłanie danych " + BestLocationNow.getProvider(), "ukończono pomyślnie");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();

                    }
                });
                requestQueue.add(jsonObjectRequest);


            }
            Log.d("Lokalizacja odrzucona", location.getProvider() + ", Acc " + location.getAccuracy());
        } else
            Log.d("loc=BestLocalisationNow", " TRUE!, nie robie nic");
    }

    public boolean setNewLocation(Location location) {
        if (isBetterLocation(location)) {
            BestLocationNow = location;
            return true;
        }
        return false;

    }

    private boolean isBetterLocation(Location location) {
        if (BestLocationNow != null) {
            long timeDelta = location.getTime() - BestLocationNow.getTime();
            boolean isNew = timeDelta > 0;
            boolean isNewer = timeDelta > CheckTime;
            boolean isOlder = timeDelta < -CheckTime;
            if (location.getAccuracy() > 700 && timeDelta > 600000) {
                Log.d("2>", "duzy rozrzut, ale przeszło");
                return true;
            }


            if (isOlder) {
                Log.d("2", "lokalizacje odrzucono, bo stara");
                return false;
            } else {

                int accuracyDelta = (int) (location.getAccuracy() - BestLocationNow.getAccuracy());
                boolean isMoreAccurate = accuracyDelta < 0;
                boolean isSlightlyLessAccurate = accuracyDelta < 10 * (1 + timeDelta / CheckTime) && !isMoreAccurate;
                boolean isSignificantlyLessAccurate = accuracyDelta > 100;


                // Check if the old and new location are from the same provider
                boolean isFromSameProvider = isSameProvider(location.getProvider(),
                        BestLocationNow.getProvider());

                // Determine location quality using a combination of timeliness and accuracy
                if (isMoreAccurate || isSlightlyLessAccurate) {
                    Log.d("2", "2");
                    return true;
                    //} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                    //   return false;
                } else if (timeDelta > CheckTime * 10 && !isSignificantlyLessAccurate) {
                    Log.d("2", "3");
                    return true;
                }
                Log.d("2", "4");
                return false;
            }
        }
        return !(location.getAccuracy() > 700);
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
        onLocationChanged(getBestLocation());
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    public void stopUsingGPS() {
        if (locationManager != null)
            locationManager.removeUpdates(GetGPS.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUsingGPS();
    }


    public double getLatitude() {
        if (BestLocationNow != null) {
            return BestLocationNow.getLatitude();
        } else return 0;
    }

    public double getLongitude() {
        if (BestLocationNow != null) {
            return BestLocationNow.getLongitude();
        } else return 0;
    }

    public double getAccuracy() {
        if (BestLocationNow != null) {
            return BestLocationNow.getAccuracy();
        } else return 0;
    }

    public double getTime() {
        if (BestLocationNow != null) {
            return BestLocationNow.getTime();
        } else return 0;

    }

    public boolean isCanGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("Internet is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void showSettingsAlertInternet() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Internet settings");

        // Setting Dialog Message
        alertDialog.setMessage("Internet provider is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    public String getAddress(double latit,double longit) {
        String errorMessage=null;
        String cAddress = "";
        if (latit == 0 && longit == 0) {
            errorMessage = "no_location_data_provided";
            Log.wtf("TAG", errorMessage);
            return "";
        }
        Geocoder geocoder = new Geocoder(mContext);//, Locale.getDefault());

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
}