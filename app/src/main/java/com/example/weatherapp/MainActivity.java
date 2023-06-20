package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.security.keystore.StrongBoxUnavailableException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {



    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBlack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d("oncreate","Worked well");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED  && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
//            Log.d("oncreate","Worked well2");
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
//            Log.d("oncreate","Worked well2");
        }
        

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location!=null){
            cityName = getCityName(location.getLongitude(), location.getLatitude());
            Log.d("oncreate","worked well2");

            Toast.makeText(this, "Location grabbed successfully", Toast.LENGTH_SHORT).show();
            getWeatherInfo(cityName);
        }
        else{
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }



        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter City name", Toast.LENGTH_SHORT).show();
                }
                else{
                    cityNameTV.setText(city);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("oncreate","Worked well2");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            Log.d("oncreate","Worked well2");
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for (Address adr: addresses){
                if (adr!=null){
                    String city = adr.getLocality();
                    if (city!=null && !city.equals("")){
                        cityName = city;
                        Log.d("TAG","CITY FOUND");
                    }
                    else{
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }
    private void getWeatherInfo(String cityName){
        String url = " https://api.weatherapi.com/v1/forecast.json?key=39bdf804dc414e7da0591806231706&q="+cityName+"&days=1&aqi=yes&alerts=yes ";
//        Toast.makeText(this, "calling api", Toast.LENGTH_SHORT).show();
        cityNameTV.setText(cityName);
        Log.d("city","setting city name");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        Toast.makeText(this, "1st request", Toast.LENGTH_SHORT).show();
        Log.d("JSON","1st request volley");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                Log.d("JSON", "2nd request");


                loadingPB.setVisibility(View.GONE);
                Log.d("JSON", "2nd request");
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
                Log.d("JSON", "2nd request");

                try {
                    String temperature = null;
                    temperature = response.getJSONObject("current").getString("temp_c");
                    Log.d("JSON", "2nd request");
                    temperatureTV.setText(temperature + "°c");
                    int isday = 0;
                    isday = response.getJSONObject("current").getInt("is_day");
                    Log.d("JSON", "day is :" + isday);
                    String condition = null;
                    condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = null;
                    conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if (isday == 1) {
                        Picasso.get().load("https://images.unsplash.com/photo-1600262912274-28f333fa17bd?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=870&q=80").into(backIV);
                    } else {
                        Picasso.get().load("https://images.unsplash.com/photo-1590418606746-018840f9cd0f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=387&q=80").into(backIV);

                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecast0.getJSONArray("hour");
                    String time = null;
                    String temper = null;
                    String img = null;
                    String wind = null;
                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourobj = hourArray.getJSONObject(i);
                        time = hourobj.getString("time");
                        temper = hourobj.getString("temp_c");
                        img = hourobj.getJSONObject("condition").getString("icon");
                        wind = hourobj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.d("JSON", "Catch of request");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "ERRORRR", Toast.LENGTH_SHORT).show();
            }



    });
        requestQueue.add(jsonObjectRequest);

//
    }
}