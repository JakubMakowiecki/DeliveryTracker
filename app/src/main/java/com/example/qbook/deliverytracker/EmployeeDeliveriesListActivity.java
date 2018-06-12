package com.example.qbook.deliverytracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDeliveriesListActivity extends AppCompatActivity implements MyEventListener {
    private RecyclerView recyclerView;
    private List<Delivery> deliveryList;
    private List<Boolean> deliveryCheckedList;
    private LayoutInflater layoutInflater;
    private CustomAdapter adapter;
    private LinearLayoutManager layoutManager;
    private FloatingActionButton floatingActionButton;
    private RequestQueue requestQueue;
    JsonArrayRequest jsonArrayRequest;
    String ServerAddress;
    User user = new User();
    List<Delivery> listOfDeliveries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_deliveries_list);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        Intent intent = getIntent();
        ServerAddress = intent.getSerializableExtra("ServerAddress").toString();
        user.login = intent.getSerializableExtra("login").toString();
        user.id = intent.getSerializableExtra("ID").toString();
        user.isPasswordEmpty = intent.getBooleanExtra("wasPasswordEmpty", true);

        listOfDeliveries = new ArrayList<>();

        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, ServerAddress + "/route/" + user.id + "/deliveryPoints", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray array) { // odpowiedz z serwera
                try {   // informacje o użytkowniku
                    Log.d("Dlugosc JSONArray", "" + array.length());
                    for (int i = 0; i < array.length(); i++) {
                        listOfDeliveries.add(new Delivery(array.getJSONObject(i).getInt("ID"),String.valueOf(array.getJSONObject(i).getDouble("Latitude")),String.valueOf(array.getJSONObject(i).getDouble("Longitude"))));

                    }
                    deliveryCheckedList = new ArrayList<>();
                    for (Delivery d : listOfDeliveries) {
                        deliveryCheckedList.add(false);
                    }
                    Log.d("zawartosc",array.toString());


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




        setListofDeliveries(listOfDeliveries);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.employee_delivery_list_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject object = new JSONObject();
                JSONArray array = new JSONArray();
                try { // pobranie parametrów do wysłania

                    for (int i = 0; i < listOfDeliveries.size(); i++) {
                        if (deliveryCheckedList.get(i).booleanValue()) {
                            //object.put("deliveryID",);
                            array.put(listOfDeliveries.get(i).getDeliveryIdAsString());
                        }
                    }
                    Log.d("lista wybranych: ", array.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

//

                Intent intent;
                intent = new Intent(EmployeeDeliveriesListActivity.this, MapsActivity.class);
                intent.putExtra("ServerAddress", ServerAddress);
                intent.putExtra("ID", user.id);
                intent.putExtra("login", user.login);
                intent.putExtra("wasPasswordEmpty", user.isPasswordEmpty);
                startActivity(intent);


//
            }
        });
    }

    public void setListofDeliveries(List<Delivery> listofDeliveries) {
        this.deliveryList = listofDeliveries;

        layoutInflater = getLayoutInflater();
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView = (RecyclerView) findViewById(R.id.delivery_list_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);

        Log.d("list of deliveries:", listofDeliveries.toString());

        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                layoutManager.getOrientation()
        );

        itemDecoration.setDrawable(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.divider
                )
        );

        recyclerView.addItemDecoration(
                itemDecoration
        );

    }

    //move to the maps activity (add loading animation)
    public void startEvent() {
        new MyAsyncTask(this).execute();
    }

    @Override
    public void onEventCompleted() {
        Intent intent;

        intent = new Intent(EmployeeDeliveriesListActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onEventFailed() {

    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = layoutInflater.inflate(R.layout.employee_delivery_list_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, final int position) {
            Delivery currentItem = deliveryList.get(position);


            holder.deliveryID.setText(currentItem.getDeliveryIdAsString());
            holder.deliveryLocationLatitude.setText(currentItem.getDeliveryLocationLatitude());
            holder.deliveryLocationLongitude.setText(currentItem.getDeliveryLocationLongitude());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        deliveryCheckedList.set(position, true);
                    } else {
                        deliveryCheckedList.set(position, false);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return (deliveryList == null) ? 0 : deliveryList.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView deliveryID;
            private TextView deliveryLocationLatitude;
            private TextView deliveryLocationLongitude;
            private CheckBox checkBox;

            private ViewGroup container;

            public CustomViewHolder(final View itemView) {
                super(itemView);

                this.deliveryID = (TextView) itemView.findViewById(R.id.package_id_value_textView);
                this.deliveryLocationLatitude = (TextView) itemView.findViewById(R.id.delivery_address_lat_value_textView);
                this.deliveryLocationLongitude = (TextView) itemView.findViewById(R.id.delivery_addres_lng_value_textView);
                this.checkBox = (CheckBox) itemView.findViewById(R.id.delivery_checkBox);


                //TODO zrobić tu zapisywanie wybranych do jakiejś tablicy i odczyt tego potem w POST'cie do sserwera

                this.container = (ViewGroup) itemView.findViewById(R.id.delivery_info_recyclerview_item_root_constraint);
                this.container.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

            }
        }

    }

    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        private MyEventListener callback;

        public MyAsyncTask(MyEventListener cb) {
            callback = cb;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (callback != null) {
                callback.onEventCompleted();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

       // adapter.notifyDataSetChanged();
    }

}
