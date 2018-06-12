package com.example.qbook.deliverytracker;

import android.content.Intent;
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

public class EmployerEmployeeListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Employee> employeeList;
    private List<Boolean> employeeCheckedList;
    private LayoutInflater layoutInflater;
    private CustomAdapter adapter;
    private LinearLayoutManager layoutManager;
    private FloatingActionButton floatingActionButton;
    private RequestQueue requestQueue;
    String ServerAddress;
    User user=new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_employee_list);

        Intent intent = getIntent();
        ServerAddress = intent.getSerializableExtra("ServerAddress").toString();
        requestQueue= Volley.newRequestQueue(getApplicationContext());
        user.login = intent.getSerializableExtra("login").toString();
        user.id = intent.getSerializableExtra("ID").toString();
        user.isPasswordEmpty = intent.getBooleanExtra("wasPasswordEmpty", true);

        final List<Employee> listOfEmployees = new ArrayList<>();
        //zrobic zapis z tego co przychodzi z intentem (z bazy danych info)
        listOfEmployees.add(new Employee(0,"Kuba", "Wojewodzki"));
        listOfEmployees.add(new Employee(1,"Marek", "Krajewski"));
        listOfEmployees.add(new Employee(2,"Jacek","Poniedziałek"));
        listOfEmployees.add(new Employee(3,"Wojtek","Smarzowski"));
        employeeCheckedList= new ArrayList<>();
        for(Employee e : listOfEmployees) {
            employeeCheckedList.add(false);
        }
        setListofEmployees(listOfEmployees);
        floatingActionButton = findViewById(R.id.employer_employee_list_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject object = new JSONObject();
                JSONArray array = new JSONArray();
                try { // pobranie parametrów do wysłania

                    for(int i = 0; i<listOfEmployees.size();i++) {
                        if(employeeCheckedList.get(i).booleanValue()) {
                            //object.put("deliveryID",);
                            array.put(listOfEmployees.get(i).getEmployeeIdString());
                        }
                    }
                    Log.d("lista wybranych: ", array.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent;
                intent = new Intent(EmployerEmployeeListActivity.this, EmployerViewActivity.class);
                intent.putExtra("ServerAddress", ServerAddress);
                intent.putExtra("ID", user.id);
                intent.putExtra("login", user.login);
                intent.putExtra("wasPasswordEmpty", user.isPasswordEmpty);
                startActivity(intent);

                String insertUrl = ServerAddress + ""; //TODO add request url
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, insertUrl, array, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //TODO check if "algorithm" is "true"
//                        Intent intent;
//                        intent = new Intent(EmployerEmployeeListActivity.this, EmployerViewActivity.class); //TODO add employer view activity
//                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("response error",error.toString());
                    }
                });
            }
        });
    }

    public void setListofEmployees(List<Employee> listofEmployees) {
        this.employeeList = listofEmployees;

        layoutInflater = getLayoutInflater();
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView = (RecyclerView) findViewById(R.id.employee_list_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);

        Log.d("list of employees:", listofEmployees.toString());

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

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = layoutInflater.inflate(R.layout.employer_employee_list_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, final int position) {
            Employee currentItem = employeeList.get(position);


            holder.employeeId.setText(currentItem.getEmployeeIdString());
            holder.employeeName.setText(currentItem.getEmployeeName()+" "+currentItem.getEmployeeSurname());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        employeeCheckedList.set(position,true);
                    } else {
                        employeeCheckedList.set(position,false);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (employeeList == null) ? 0 : employeeList.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView employeeId;
            private TextView employeeName;
            private CheckBox checkBox;

            private ViewGroup container;

            public CustomViewHolder(View itemView) {
                super(itemView);

                this.employeeId = (TextView) itemView.findViewById(R.id.employee_id_value_textView);
                this.employeeName = (TextView) itemView.findViewById(R.id.employee_name_value_textView);
                this.checkBox = (CheckBox) itemView.findViewById(R.id.employee_checkBox);
                this.container = (ViewGroup) itemView.findViewById(R.id.employee_info_recyclerview_item_root_constraint);
                this.container.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, GoalActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(intent);
            }
        }

    }
}
