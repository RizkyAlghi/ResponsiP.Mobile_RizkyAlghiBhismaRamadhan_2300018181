package com.pmob.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.*;

import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity implements com.google.android.gms.maps.OnMapReadyCallback {

    private TextView cityNameText, temperatureText, humidityText, descriptionText, windText, tvUpdate;
    private ImageView weatherIcon;
    private Button refreshButton;
    private EditText cityNameInput;
    private com.google.android.gms.maps.GoogleMap mMap;
    private DatabaseHelper dbHelper;

    private static final String API_KEY = "7be4a25466b8361c2ae28097a6aa5617"; // API Cuaca

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Database (Syarat CRUD)
        dbHelper = new DatabaseHelper(this);

        // Link UI
        cityNameText = findViewById(R.id.cityNameText);
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        descriptionText = findViewById(R.id.descriptionText);
        weatherIcon = findViewById(R.id.weatherIcon);
        refreshButton = findViewById(R.id.fetchWeatherButton);
        cityNameInput = findViewById(R.id.cityNameInput);
        tvUpdate = findViewById(R.id.tvUpdate);

        // Inisialisasi Map (Syarat Google Map API)
        com.google.android.gms.maps.SupportMapFragment mapFragment = (com.google.android.gms.maps.SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) { mapFragment.getMapAsync(this); }

        refreshButton.setOnClickListener(view -> {
            String cityName = cityNameInput.getText().toString();
            if (!cityName.isEmpty()) {
                // 1. Ambil data Cuaca dari API
                FetchWeatherData(cityName);

                // 2. Simpan ke Database (Fitur CRUD - Create)
                dbHelper.addHistory(cityName);

                // --- BAGIAN PENGECEKAN DATABASE ---
                // Mengambil semua data untuk membuktikan CRUD jalan
                int totalData = dbHelper.getAllHistory().size();
                Toast.makeText(this, "Tersimpan! Total riwayat di Database: " + totalData, Toast.LENGTH_SHORT).show();
                // ---------------------------------
            } else {
                Toast.makeText(this, "Masukkan nama kota!", Toast.LENGTH_SHORT).show();
            }
        });

        // Load data terakhir dari database (Fitur CRUD - Read)
        // Ini membuktikan data tidak hilang saat aplikasi ditutup
        FetchWeatherData(dbHelper.getLastCity());
    }

    private void FetchWeatherData(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric&lang=id";

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                runOnUiThread(() -> {
                    updateUI(result);
                    updateMapLocation(cityName); // Update Map saat cuaca dicari
                });
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    private void updateUI(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject main = jsonObject.getJSONObject("main");
            double temp = main.getDouble("temp");
            double hum = main.getDouble("humidity");
            double wind = jsonObject.getJSONObject("wind").getDouble("speed");
            String desc = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

            cityNameText.setText(jsonObject.getString("name"));
            temperatureText.setText(String.format("%.0f°", temp));
            humidityText.setText("Kelembapan: " + hum + "%");
            windText.setText("Angin: " + wind + " km/h");
            descriptionText.setText(desc);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvUpdate.setText("Update: " + sdf.format(new Date()));
        } catch (JSONException e) { e.printStackTrace(); }
    }

    // LOGIKA MAP API
    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation("Jakarta"); // Default awal
    }

    private void updateMapLocation(String cityName) {
        if (mMap == null) return;
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                com.google.android.gms.maps.model.LatLng location = new com.google.android.gms.maps.model.LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();
                mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions().position(location).title("Cuaca di " + cityName));
                mMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 10f));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
