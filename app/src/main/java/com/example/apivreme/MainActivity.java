package com.example.apivreme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;


@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_LOCATION = 1;
    double latti, longi;
    TextView cityName;
    ImageButton searchButton, locationButton;
    TextView result;
    ImageView image;
    String latitude;
    String longitude;
    String message;
    LocationManager locationManager;
    AutoCompleteTextView autoComplete;

    public void goToMap(View view) throws JSONException, ExecutionException, InterruptedException {
        if(autoComplete.getText().toString().equals(""))
        {
            onClick(view);
            Log.v(String.valueOf(longi),"long");
            Log.v(String.valueOf(latti),"latti");
            message = String.valueOf(latti) + " " + String.valueOf(longi);
        }  else {
            Weather weather = new Weather();
            message = autoComplete.getText().toString();
            String content = weather.execute("https://openweathermap.org/data/2.5/weather?q=" +
                    message+"&appid=b6907d289e10d714a6e88b30761fae22").get();
            JSONObject object = new JSONObject(content);
            String coord =  object.getString("coord");
            JSONObject coordPart = new JSONObject(coord);
            longitude = coordPart.getString("lon");
            latitude = coordPart.getString("lat");
            message = latitude + " " + longitude;
            System.out.println(message + " long, lat");
        }
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("EXTRA_MESSAGE", message);
        startActivity(intent);
    }
    public void locate(View view) {
        locationButton = findViewById(R.id.mylocation);
        locationButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        }
    }
    public void stavi(double longitude, double latitude){
        result = findViewById(R.id.result);
        image = findViewById(R.id.icon);
        String content;
        Weather weather = new Weather();
        try {
            content = weather.execute("https://openweathermap.org/data/2.5/weather?lat=" +
                    latitude+"&lon=" + longitude + "&appid=b6907d289e10d714a6e88b30761fae22").get();
            JSONObject object = new JSONObject(content);
            String weatherData = object.getString("weather"); //WEATHER
            String mainTemperature = object.getString("main"); // MAIN
            JSONArray array = new JSONArray(weatherData);
            String main="",desc="",name="";
            name = object.getString("name");
            autoComplete.setText(name);
            autoComplete.dismissDropDown();
            for (int i = 0; i < array.length(); i++){
                JSONObject weatherPart = array.getJSONObject(i);
                main = weatherPart.getString("main");
                desc = weatherPart.getString("description");
            }
            JSONObject mainPart = new JSONObject(mainTemperature);
            int temperature = mainPart.getInt("temp");
            int timeZone = object.getInt("timezone");
            System.out.println(timeZone);
            namestiSlika(main, temperature, longitude, latitude, timeZone);
            String resultText = "Current location: " + name + "\nTemperature: " + temperature + "°C\nDescription: " + desc;
            result.setText(resultText);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void namestiSlika(String main, int temperature, double longi, double latti, int timeZone) throws ExecutionException, InterruptedException, JSONException {
        int dif = timeZone/3600;
        TimeZone tz = TimeZone.getTimeZone("GMT+"+ String.format("%02d",dif)+":00");
        Calendar c = Calendar.getInstance(tz);
        String time = String.format("%02d" , c.get(Calendar.HOUR_OF_DAY))+":"+
                String.format("%02d" , c.get(Calendar.MINUTE))+":"+String.format("%02d" , c.get(Calendar.SECOND));
        System.out.println(time);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if(hour >=6 && hour <= 19) {
            if (main.equals("Clear")) {
                if (temperature > 30) {
                    image.setVisibility(View.VISIBLE);
                    image.setImageResource(R.mipmap.hot);
                } else {
                    image.setVisibility(View.VISIBLE);
                    image.setImageResource(R.mipmap.orange);
                }
            } else if (main.equals("Clouds") || main.equals("Mist")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.clouds);
            } else if (main.equals("Rain")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.rain);
            } else if (main.equals("Thunderstorm")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.thunder);
            } else if (main.equals("Snow")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.snow);
            }
        } else {
            if (main.equals("Clear")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.clearnight);
            } else if (main.equals("Clouds") || main.equals("Mist")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.cloudsnight);
            } else if (main.equals("Rain")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.rainnight);
            } else if (main.equals("Thunderstorm")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.thundernight);
            } else if (main.equals("Snow")) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(R.mipmap.snow);
            }
        }
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                latti = location.getLatitude();
                longi = location.getLongitude();
                latitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                stavi(longi, latti);
            } else if (location1 != null) {
                latti = location1.getLatitude();
                longi = location1.getLongitude();
                latitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                stavi(longi, latti);
            } else  if (location2 != null) {
                latti = location2.getLatitude();
                longi = location2.getLongitude();
                latitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                stavi(longi, latti);
        }else{
                Toast.makeText(this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    class Weather extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                int data = isr.read();
                String content = "";
                char ch;
                while(data != -1){
                    ch = (char) data;
                    content = content + String.valueOf(ch);
                    data = isr.read();
                }
                return content;
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    public void search(View view){
        searchButton = findViewById(R.id.searchButton);
        result = findViewById(R.id.result);
        image = findViewById(R.id.icon);
        String written = autoComplete.getText().toString();
        String content;
        Weather weather = new Weather();
        try {
            content = weather.execute("https://openweathermap.org/data/2.5/weather?q=" +
                    written+"&appid=b6907d289e10d714a6e88b30761fae22").get();
            JSONObject object = new JSONObject(content);
            String weatherData = object.getString("weather");
            String mainTemperature = object.getString("main");
            JSONArray array = new JSONArray(weatherData);
            String main="",desc="";
            for (int i = 0; i < array.length(); i++){
                JSONObject weatherPart = array.getJSONObject(i);
                main = weatherPart.getString("main");
                desc = weatherPart.getString("description");

            }
            JSONObject mainPart = new JSONObject(mainTemperature);
            int temperature = mainPart.getInt("temp");
            String coord = object.getString("coord");
            JSONObject coordObject = new JSONObject(coord);
            double longi = coordObject.getDouble("lon");
            int timeZone = object.getInt("timezone");
            double lat = coordObject.getDouble("lat");
           // System.out.println(longi + ", " + lat + " " + timeZone);
            namestiSlika(main, temperature, longi, lat, timeZone);
            String resultText = "City: " + object.getString("name") + "\nTemperature: " + temperature + "°C\nDescription: " + desc;
            result.setText(resultText);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.i("CITIES",loadJSONFromAsset());
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        String[] gradovi = dajMiGradovi(loadJSONFromAsset());
        AutoCompleteTextView ac = findViewById(R.id.autoComplete);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gradovi);
        ac.setAdapter(adapter);
        locationButton = findViewById(R.id.mylocation);
        locationButton.setOnClickListener((View.OnClickListener) this);
        autoComplete = findViewById(R.id.autoComplete);
    }
    private String[] dajMiGradovi(String loadJSONFromAsset) {
        String[] gradovi = null;
        try {
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset());
            gradovi = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                gradovi[i] = jsonObject.getString("name");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gradovi;
    }
}
