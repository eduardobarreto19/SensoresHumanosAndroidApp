package com.example.sensoreshumanos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataGeneratorActivity extends AppCompatActivity {

    private Button logout;
    private Button startRecord;
    private FirebaseAuth mAuth;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mAmbientTemperature, mLight, mPressure, mRelativeHumidity, mProximity;
    private SensorEventListener mAccelerometerListener, mAmbientTemperatureListener, mLightListener, mPressureListener, mRelativeHumidityListener, mProximityListener;
    private List<List<String>> accelerometerRecord, ambientTemperatureRecord,lightRecord, pressureRecord, relativeHumidityRecord, proximityRecord;

    TextView latitude, longitude, altitude, distance, accelerometerX, accelerometerY, accelerometerZ;
    static final int PERMISSION_LOCATION_ID = 5;
    static final int REQUEST_CHECK_SETTINGS = 6;
    static final double RADIUS_OF_EARTH_KM = 6378.1;
    Location location;
    double latLabs = 4.627283;
    double longLabs = -74.064375;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    FirebaseFirestore db;

    public DataGeneratorActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_generator_acitivity);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        logout = findViewById(R.id.logout_button);
        startRecord = findViewById(R.id.start_button);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        altitude = findViewById(R.id.altitude);
        distance = findViewById(R.id.distance);
        accelerometerX = findViewById(R.id.accelerometerX);
        accelerometerY = findViewById(R.id.accelerometerY);
        accelerometerZ = findViewById(R.id.accelerometerZ);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAmbientTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mRelativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        accelerometerRecord = new ArrayList<>();
        ambientTemperatureRecord = new ArrayList<>();
        lightRecord = new ArrayList<>();
        pressureRecord = new ArrayList<>();
        relativeHumidityRecord = new ArrayList<>();
        proximityRecord = new ArrayList<>();

        mAccelerometerListener = new SensorEventListener() {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            @Override
            public void onSensorChanged(SensorEvent event) {
                accelerometerX.setText(String.valueOf(event.values[0]));
                accelerometerY.setText(String.valueOf(event.values[1]));
                accelerometerRecord.add(Arrays.asList(ts,String.valueOf(event.values[0]),String.valueOf(event.values[1]),String.valueOf(event.values[2])));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mLightListener = new SensorEventListener() {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            @Override
            public void onSensorChanged(SensorEvent event) {
                accelerometerZ.setText(String.valueOf(event.values[0]));
                lightRecord.add(Arrays.asList(ts,String.valueOf(event.values[0])));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mAmbientTemperatureListener = new SensorEventListener() {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            @Override
            public void onSensorChanged(SensorEvent event) {
                //accelerometerZ.setText(String.valueOf(event.values[0]));
                ambientTemperatureRecord.add(Arrays.asList(ts,String.valueOf(event.values[0])));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mPressureListener = new SensorEventListener() {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            @Override
            public void onSensorChanged(SensorEvent event) {
                //accelerometerZ.setText(String.valueOf(event.values[0]));
                pressureRecord.add(Arrays.asList(ts,String.valueOf(event.values[0])));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mRelativeHumidityListener = new SensorEventListener() {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            @Override
            public void onSensorChanged(SensorEvent event) {
                //accelerometerZ.setText(String.valueOf(event.values[0]));
                relativeHumidityRecord.add(Arrays.asList(ts,String.valueOf(event.values[0])));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mProximityListener = new SensorEventListener() {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            @Override
            public void onSensorChanged(SensorEvent event) {
                //accelerometerZ.setText(String.valueOf(event.values[0]));
                proximityRecord.add(Arrays.asList(ts,String.valueOf(event.values[0])));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        //localizacion
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.i("LOCATION", "Location update in the callback: " + location);
                if (location != null) {
                    latitude.setText(String.valueOf(location.getLatitude()));
                    longitude.setText(String.valueOf(location.getLongitude()));
                    altitude.setText(String.valueOf(location.getAltitude()));
                    distance.setText(String.valueOf(calculateDistance(latLabs, longLabs, location.getLatitude(), location.getLongitude())));
                }
            }
        };

        //Permission location
        requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, "Required for distance", PERMISSION_LOCATION_ID);
        settingsLocation();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
                Runnable runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", mAuth.getCurrentUser().getEmail());
                        user.put("fullName", "Juan David Rodriguez Arevalo");
                        user.put("timestamp", ts);
                        user.put("lat", latitude.getText());
                        user.put("long", longitude.getText());
                        user.put("distance", distance.getText());
                        //user.put("accelerometer", RecordToMapXYZ(accelerometerRecord));
                        //user.put("ambientTemperature", RecordToMap(ambientTemperatureRecord));
                        //user.put("light", RecordToMap(lightRecord));
                        //user.put("pressure", RecordToMap(pressureRecord));
                        user.put("relativeHumidity", RecordToMap(relativeHumidityRecord));
                        //user.put("proximity", RecordToMap(proximityRecord));
                        accelerometerRecord.clear();
                        ambientTemperatureRecord.clear();
                        lightRecord.clear();
                        pressureRecord.clear();
                        relativeHumidityRecord.clear();
                        proximityRecord.clear();
                        Log.d("DB", "Si llego");

                        db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("sensors")
                                .add(user)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("DB", "DocumentSnapshot added with ID: " + documentReference.getId() + " added data: " + "recordData");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("DB", "Error adding document", e);
                                    }
                                });
                    }
                };
                exec.scheduleAtFixedRate(runnable1,0,5, TimeUnit.SECONDS);
            }
        });

    }

    private List<Map<String, Object>> RecordToMap(List<List<String>> record){
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (List<String> i: record
        ) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("timestamp", i.get(0));
            recordMap.put("data", i.get(1));
            recordList.add(recordMap);
        }
        return recordList;
    }

    private List<Map<String, Object>> RecordToMapXYZ(List<List<String>> record){
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (List<String> i: record
        ) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("timestamp", i.get(0));
            recordMap.put("data", i.get(1));
            recordMap.put("data", i.get(2));
            recordMap.put("data", i.get(3));
            recordList.add(recordMap);
        }
        return recordList;
    }

    private LocationRequest createLocationRequest(){
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); //tasa de refresco en milisegundos
        mLocationRequest.setFastestInterval(5000); // máxima tasa de refresco
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void startLocationUpdates() {
        //Verificación de permiso!!
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==	PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        mSensorManager.unregisterListener(mAccelerometerListener);
        mSensorManager.unregisterListener(mLightListener);
        mSensorManager.unregisterListener(mPressureListener);
        mSensorManager.unregisterListener(mAmbientTemperatureListener);
        mSensorManager.unregisterListener(mRelativeHumidityListener);
        mSensorManager.unregisterListener(mProximityListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsLocation();
        mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mLightListener, mLight, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mPressureListener, mPressure, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mAmbientTemperatureListener, mAmbientTemperature, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mRelativeHumidityListener, mRelativeHumidity, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mProximityListener, mProximity, mSensorManager.SENSOR_DELAY_NORMAL);
    }

    private void settingsLocation(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates(); //Todas las condiciones para recibir localizaciones
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(DataGeneratorActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        } break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(DataGeneratorActivity.this, "Impossible to get a location", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private double calculateDistance(double lat1, double long1, double lat2, double long2){
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }

    /**
     * Metodo para solicitar un permiso
     * @param context actividad actual
     * @param permission permiso que se desea solicitar
     * @param just justificacion para el permiso
     * @param id identificador con el se marca la solicitud y se captura el callback de respuesta
     */
    public void requestPermission(Activity context, String permission, String just, int id) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                // Show an expanation to the user *asynchronously*   
                Toast.makeText(context, just, Toast.LENGTH_LONG).show();
            }
            // request the permission.   
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        settingsLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();  //Se encendió la localización!!!
                } else {
                    Toast.makeText(this, "Sin acceso a localización, hardware deshabilitado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}