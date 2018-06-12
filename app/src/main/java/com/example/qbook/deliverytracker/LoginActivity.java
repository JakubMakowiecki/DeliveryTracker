package com.example.qbook.deliverytracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

//import android.support.design.widget.Snackbar;



public class LoginActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    EditText clientid;
    EditText password;
    TextView pole1, pole2;
    JsonArrayRequest jsonArrayRequest;
    User user=new User();
    final Activity activity = this;
    String ServerAddress=  "http://192.168.1.25:8080";
    //String ServerAddress=  "http://192.168.43.26:8080";

    @Override // metoda onCreate
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        clientid = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        user = new User();
    }

    //Login - zalogowanie użytkownika
    public void onClickLogin(View view) {


        if (clientid.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "Fill login field!", Toast.LENGTH_LONG).show();
        } else {
            if (password.getText().length() == 0) {
                Toast.makeText(getApplicationContext(), "password not present! You will be able to display trace only", Toast.LENGTH_SHORT).show();
                user.isPasswordEmpty = true;
                Log.d("Hasło", "Puste");
            } else {
                user.isPasswordEmpty = false;
                Log.d("Hasło", "Wypełnione");
            }

            //Utworzenie JSONObject z danymi logowania
            JSONObject object = new JSONObject();
            try { // pobranie parametrów do wysłania
                object.put("login", clientid.getText());
                if (!user.isPasswordEmpty)
                    object.put("password", password.getText());
                else
                    object.put("password", 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //POST - wysyłanie danych logowania na serwer
            String insertUrl = ServerAddress + "/login";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, insertUrl, object,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if (response.has("delivererID")) {
                                    int delivererID=response.getInt("delivererID");
                                    Intent intent = new Intent(getApplicationContext(), CustomerViewActivity.class);
                                    intent.putExtra("ServerAddress", ServerAddress);
                                    //intent.putExtra("requestQueue",requestQueue);

                                    intent.putExtra("delivererID", delivererID);
                                    //dialog.cancel();
                                    startActivity(intent);
                                } else {


                                    //Pobranie informacji o użytkowniku z serwera
                                    String showUrl = ServerAddress + "/login/" + clientid.getText().toString();
                                final JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.GET, showUrl, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject e) { // odpowiedz z serwera
                                        try {   // informacje o użytkowniku


                                            //JSONObject e = response.getJSONObject("response");
                                            Log.d("log:", e.toString());
                                            user.login = e.getString("login");
                                            Log.d("login into TRY", user.login);
                                            user.id = e.getString("ID");
                                            user.type = e.getInt("type");
                                            if (user.type == 0) {
                                                Intent intent = new Intent(getApplicationContext(), CustomerViewActivity.class);
                                                intent.putExtra("ServerAddress", ServerAddress);
                                                //intent.putExtra("requestQueue",requestQueue);
                                                intent.putExtra("deliveryID", user.id);
                                                //dialog.cancel();
                                                startActivity(intent);
                                            } else if (user.type == 2) {
                                                Intent intent = new Intent(getApplicationContext(), EmployerEmployeeListActivity.class);
                                                intent.putExtra("ServerAddress", ServerAddress);
                                                //intent.putExtra("requestQueue",requestQueue);
                                                intent.putExtra("ID", user.id);
                                                intent.putExtra("login", user.login);
                                                intent.putExtra("wasPasswordEmpty", user.isPasswordEmpty);
                                                //dialog.cancel();
                                                startActivity(intent);
                                            } else if (user.type == 1) {
                                                Intent intent = new Intent(getApplicationContext(), EmployeeDeliveriesListActivity.class);
                                                intent.putExtra("ServerAddress", ServerAddress);
                                                //intent.putExtra("requestQueue",requestQueue);
                                                intent.putExtra("ID", user.id);
                                                intent.putExtra("login", user.login);
                                                intent.putExtra("wasPasswordEmpty", user.isPasswordEmpty);
                                                startActivity(intent);
                                            }

                                        } catch (Exception e1) {
                                            //pole2.setText(e.toString());
                                            Log.e("fdfdfd", e1.toString());
                                            e1.printStackTrace();
                                            Toast.makeText(getApplicationContext(), "Ocurred error with starting next Activity, ", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getApplicationContext(), "Server unavailable\nPlease check your connection", Toast.LENGTH_LONG).show();
                                    }
                                });
                                requestQueue.add(jsonObjectRequest2);
                            }
                                } catch(Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "URL incorrect", Toast.LENGTH_LONG).show();
                                }
                            }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Login or Passsword incorrect\nIf you want to track deliverer, remember to leave field password empty", Toast.LENGTH_LONG).show();
                    //dialog.cancelPendingInputEvents();// cancel();
                }
            });
            requestQueue.add(jsonObjectRequest);

        }
    }
}
