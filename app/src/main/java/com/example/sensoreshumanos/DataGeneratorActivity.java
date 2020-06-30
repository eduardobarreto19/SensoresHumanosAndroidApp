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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    TextView latitude, longitude, altitude, distance, accelerometerX, accelerometerY, accelerometerZ, ambientTemperature, light, pressure, relativeHumidity, proximity;
    float deltaAccelerometerX, deltaAccelerometerY, deltaAccelerometerZ, deltaAmbientTemperature, deltaLight, deltaPressure, deltaRelativeHumidity, deltaProximity;
    double deltaLat, deltaLong, deltaAltitude, deltaDistance;
    float lastAccelerometerX = 0.0f , lastAccelerometerY = 0.0f , lastAccelerometerZ = 0.0f , lastAmbientTemperature = 0.0f , lastLight = 0.0f , lastPressure = 0.0f , lastRelativeHumidity = 0.0f , lastProximity = 0.0f ;
    double lastLat = 0.0 , lastLong = 0.0 , lastAltitude = 0.0 , lastDistance = 0.0;
    static final int PERMISSION_LOCATION_ID = 5;
    static final int REQUEST_CHECK_SETTINGS = 6;
    double latLabs = 4.627283;
    double longLabs = -74.064375;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_generator_acitivity);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        logout = findViewById(R.id.logout_button);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        altitude = findViewById(R.id.altitude);
        distance = findViewById(R.id.distance);
        accelerometerX = findViewById(R.id.accelerometerX);
        accelerometerY = findViewById(R.id.accelerometerY);
        accelerometerZ = findViewById(R.id.accelerometerZ);
        ambientTemperature = findViewById(R.id.ambienteTemperature);
        light = findViewById(R.id.light);
        pressure = findViewById(R.id.pressure);
        relativeHumidity = findViewById(R.id.relativeHumidity);
        proximity = findViewById(R.id.proximity);

        InitializeDeltas();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAmbientTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mRelativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mAccelerometerListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                accelerometerX.setText(String.valueOf(event.values[0]));
                accelerometerY.setText(String.valueOf(event.values[1]));
                accelerometerZ.setText(String.valueOf(event.values[2]));
                if(Math.abs(event.values[0] - lastAccelerometerX) > deltaAccelerometerX)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("accelerometerX")
                            .add(dataRecord)
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
                    lastAccelerometerX = event.values[0];
                }
                if(Math.abs(event.values[1] - lastAccelerometerY) > deltaAccelerometerY)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[1]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("accelerometerY")
                            .add(dataRecord)
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
                    lastAccelerometerY = event.values[1];
                }
                if(Math.abs(event.values[2] - lastAccelerometerZ) > deltaAccelerometerZ)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[2]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("accelerometerZ")
                            .add(dataRecord)
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
                    lastAccelerometerZ = event.values[2];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mLightListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                light.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastLight) > deltaLight)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("light")
                            .add(dataRecord)
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
                    lastLight = event.values[0];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mAmbientTemperatureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                ambientTemperature.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastAmbientTemperature) > deltaAmbientTemperature)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("ambientTemperature")
                            .add(dataRecord)
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
                    lastAmbientTemperature = event.values[0];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mPressureListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                pressure.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastPressure) > deltaPressure)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("pressure")
                            .add(dataRecord)
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
                    lastPressure = event.values[0];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mRelativeHumidityListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                relativeHumidity.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastRelativeHumidity) > deltaRelativeHumidity)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("relativeHumidity")
                            .add(dataRecord)
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
                    lastRelativeHumidity = event.values[0];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mProximityListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                proximity.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastProximity) > deltaProximity)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("timestamp", ts);
                    db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("proximity")
                            .add(dataRecord)
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
                    lastProximity = event.values[0];
                }
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
                    float resultsDistance[] = new float[3];
                    Location.distanceBetween(latLabs, longLabs, location.getLatitude(), location.getLongitude(), resultsDistance);
                    distance.setText(String.valueOf(resultsDistance[0]));

                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    if(Math.abs(location.getLatitude() - lastLat) > deltaLat)
                    {
                        Map<String, Object> dataRecord = new HashMap<>();
                        dataRecord.put("data", location.getLatitude());
                        dataRecord.put("timestamp", ts);
                        db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("latitude")
                                .add(dataRecord)
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
                        lastLat = location.getLatitude();
                    }
                    if(Math.abs(location.getLongitude() - lastLong) > deltaLong)
                    {
                        Map<String, Object> dataRecord = new HashMap<>();
                        dataRecord.put("data", location.getLongitude());
                        dataRecord.put("timestamp", ts);
                        db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("longitude")
                                .add(dataRecord)
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
                        lastLong = location.getLongitude();
                    }
                    if(Math.abs(location.getAltitude() - lastAltitude) > deltaAltitude)
                    {
                        Map<String, Object> dataRecord = new HashMap<>();
                        dataRecord.put("data", location.getAltitude());
                        dataRecord.put("timestamp", ts);
                        db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("altitude")
                                .add(dataRecord)
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
                        lastAltitude = location.getAltitude();
                    }
                    if(Math.abs(resultsDistance[0] - lastDistance) > deltaDistance)
                    {
                        Map<String, Object> dataRecord = new HashMap<>();
                        dataRecord.put("data", resultsDistance[0]);
                        dataRecord.put("timestamp", ts);
                        db.collection("users").document(mAuth.getCurrentUser().getEmail()).collection("distance")
                                .add(dataRecord)
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
                        lastDistance = resultsDistance[0];
                    }
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
    }

    private void InitializeDeltas(){
        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DB", "DocumentSnapshot data: " + document.getData());
                        Map<String, Object> refData = document.getData();
                        deltaLat = (double) refData.get("deltaLat");
                        deltaLong = (double) refData.get("deltaLong");
                        deltaAltitude = (double)refData.get("deltaAltitude");
                        deltaDistance = (double)refData.get("deltaDistance");
                        deltaAccelerometerX = (float)(double)refData.get("deltaAccelerometerX");
                        deltaAccelerometerY = (float)(double)refData.get("deltaAccelerometerY");
                        deltaAccelerometerZ = (float)(double)refData.get("deltaAccelerometerZ");
                        deltaAmbientTemperature = (float)(double)refData.get("deltaAmbientTemperature");
                        deltaLight = (float)(double)refData.get("deltaLight");
                        deltaPressure = (float)(double)refData.get("deltaPressure");
                        deltaRelativeHumidity = (float)(double)refData.get("deltaRelativeHumidity");
                        deltaProximity = (float)(double)refData.get("deltaProximity");

                    } else {
                        Log.d("DB", "No such document");
                    }
                } else {
                    Log.d("DB", "get failed with ", task.getException());
                }
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