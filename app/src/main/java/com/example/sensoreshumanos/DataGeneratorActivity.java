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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataGeneratorActivity extends AppCompatActivity {

    private Button logout;
    private Button startRecord;
    private FirebaseAuth mAuth;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mAmbientTemperature, mLight, mPressure, mRelativeHumidity, mProximity;
    private SensorEventListener mAccelerometerListener, mAmbientTemperatureListener, mLightListener, mPressureListener, mRelativeHumidityListener, mProximityListener;

    TextView latitude, longitude, distance, accelerometerX, accelerometerY, accelerometerZ, ambientTemperature, light, pressure, relativeHumidity, proximity;
    double deltaAccelerometerX, deltaAccelerometerY, deltaAccelerometerZ, deltaAmbientTemperature, deltaLight, deltaPressure, deltaRelativeHumidity, deltaProximity;
    double deltaDistance;
    double lastAccelerometerX = 0.0f , lastAccelerometerY = 0.0f , lastAccelerometerZ = 0.0f , lastAmbientTemperature = 0.0f , lastLight = 0.0f , lastPressure = 0.0f , lastRelativeHumidity = 0.0f , lastProximity = 0.0f ;
    double lastDistanceToHospital = 0.0;
    static final String OUT_OF_THE_CAMPUS = "Fuera del campus";
    static final String INSIDE_THE_CAMPUS = "Campus universitario";
    static final String INSIDE_BARON = "Edificio Baron";
    static final String INSIDE_GIRALDO = "Edificio Giraldo";
    static final String INSIDE_LENGUAS = "Edificio Lenguas";
    String lastKnownArea = "";
    GeoPoint lastLocation = new GeoPoint(0,0);
    static final int PERMISSION_LOCATION_ID = 5;
    static final int REQUEST_CHECK_SETTINGS = 6;
    static final float DISTANCE_UNIVERSITY = 250.0f;
    static final float DISTANCE_BUILDING = 30.0f;
    double latHospital = 4.628462, latBaron = 4.6265, latLenguas = 4.6287, latGiraldo = 4.6267;
    double longHospital = -74.063785, longBaron = -74.0638, longLenguas = -74.0628, longGiraldo = -74.0648;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    FirebaseFirestore db;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_generator_acitivity);

        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();

        logout = findViewById(R.id.logout_button);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
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
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAmbientTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mRelativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mAccelerometerListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                Timestamp ts = new Timestamp(new Date());
                accelerometerX.setText(String.valueOf(event.values[0]));
                accelerometerY.setText(String.valueOf(event.values[1]));
                accelerometerZ.setText(String.valueOf(event.values[2]));
                if(Math.abs(event.values[0] - lastAccelerometerX) > deltaAccelerometerX)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("AccelerometerX")
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
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("AccelerometerY")
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
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("AccelerometerZ")
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
                Timestamp ts = new Timestamp(new Date());

                light.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastLight) > deltaLight)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("Light")
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
                Timestamp ts = new Timestamp(new Date());
                ambientTemperature.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastAmbientTemperature) > deltaAmbientTemperature)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("AmbientTemperature")
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
                Timestamp ts = new Timestamp(new Date());

                pressure.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastPressure) > deltaPressure)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("Pressure")
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
                Timestamp ts = new Timestamp(new Date());

                relativeHumidity.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastRelativeHumidity) > deltaRelativeHumidity)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("RelativeHumidity")
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
                Timestamp ts = new Timestamp(new Date());

                proximity.setText(String.valueOf(event.values[0]));
                if(Math.abs(event.values[0] - lastProximity) > deltaProximity)
                {
                    Map<String, Object> dataRecord = new HashMap<>();
                    dataRecord.put("data", event.values[0]);
                    dataRecord.put("created_at", ts);
                    db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("Proximity")
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

                    float resultsDistanceHospital[] = new float[3];
                    Location.distanceBetween(latHospital, longHospital, location.getLatitude(), location.getLongitude(), resultsDistanceHospital);
                    float distanceToHospital = resultsDistanceHospital[0];
                    distance.setText(String.valueOf(distanceToHospital));

                    float resultsDistance[] = new float[3];
                    Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), location.getLatitude(), location.getLongitude(), resultsDistance);
                    float distanceToLastPoint = resultsDistance[0];

                    String area = getArea(location);
                    Log.d("DB", "Area: " + area);
                    Timestamp ts = new Timestamp(new Date());
                    if(!lastKnownArea.equals(area))
                    {
                        Map<String, Object> lastAreaRecord = new HashMap<>();
                        lastAreaRecord.put("last_area", area);
                        lastAreaRecord.put("last_area_at",ts);
                        db.collection("sensores humanos").document("dispositivos moviles").collection("morado").document("capa morada")
                                .collection("users").document(mAuth.getCurrentUser().getEmail()).set(lastAreaRecord, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DB", "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("DB", "Error writing document", e);
                                    }
                                });

                        // AREA EVENTS
                        Map<String, Object> AreaRecord = new HashMap<>();
                        AreaRecord.put("area_event", area);
                        AreaRecord.put("area_event_at",ts);
                        db.collection("sensores humanos").document("dispositivos moviles").collection("morado")
                                .document("capa morada").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("AreaEvents")
                                .add(AreaRecord)
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
                        lastKnownArea = area;
                    }


                    if(Math.abs(distanceToLastPoint) > deltaDistance)
                    {
                        Map<String, Object> dataRecord = new HashMap<>();
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
                        dataRecord.put("data", geoPoint);
                        dataRecord.put("created_at", ts);
                        db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("Location")
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
                        lastLocation = geoPoint;
                        Map<String, Object> lastLocationRecord = new HashMap<>();
                        lastLocationRecord.put("last_location",lastLocation);
                        lastLocationRecord.put("last_location_at",ts);
                        db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).set(lastLocationRecord, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DB", "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("DB", "Error writing document", e);
                                    }
                                });
                    }

                    if(Math.abs(distanceToHospital - lastDistanceToHospital) > deltaDistance)
                    {
                        Map<String, Object> dataRecord = new HashMap<>();
                        dataRecord.put("data", distanceToHospital);
                        dataRecord.put("created_at", ts);
                        db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).collection("DistanceToHospital")
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
                        lastDistanceToHospital = distanceToHospital;
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


    private String getArea(Location location){

        float resultsDistanceHospital[] = new float[3];
        Location.distanceBetween(latHospital, longHospital, location.getLatitude(), location.getLongitude(), resultsDistanceHospital);
        float distanceToHospital = resultsDistanceHospital[0];

        float resultsDistanceBaron[] = new float[3];
        Location.distanceBetween(latBaron, longBaron, location.getLatitude(), location.getLongitude(), resultsDistanceBaron);
        float distanceToBaron = resultsDistanceBaron[0];

        float resultsDistanceGiraldo[] = new float[3];
        Location.distanceBetween(latGiraldo, longGiraldo, location.getLatitude(), location.getLongitude(), resultsDistanceGiraldo);
        float distanceToGiraldo = resultsDistanceGiraldo[0];

        float resultsDistanceLenguas[] = new float[3];
        Location.distanceBetween(latLenguas, longLenguas, location.getLatitude(), location.getLongitude(), resultsDistanceLenguas);
        float distanceToLenguas = resultsDistanceLenguas[0];

        Log.d("DB", "Distance to Hospital: " + distanceToHospital);
        Log.d("DB", "Distance to Baron: " + distanceToBaron);
        Log.d("DB", "Distance to Giraldo: " + distanceToGiraldo);
        Log.d("DB", "Distance to Lenguas: " + distanceToLenguas);

        if(distanceToHospital <= DISTANCE_UNIVERSITY)
        {
            if(distanceToBaron <= DISTANCE_BUILDING)
            {
                return INSIDE_BARON;
            }

            if(distanceToGiraldo <= DISTANCE_BUILDING)
            {
                return INSIDE_GIRALDO;
            }

            if(distanceToLenguas <= DISTANCE_BUILDING)
            {
                return INSIDE_LENGUAS;
            }
            return INSIDE_THE_CAMPUS;
        }
        return OUT_OF_THE_CAMPUS;
    }

    private void InitializeDeltas(){
        DocumentReference docRef = db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DB", "DocumentSnapshot data: " + document.getData());
                        Map<String, Object> refData = document.getData();
                        deltaDistance = (double)refData.get("delta_distance");
                        deltaAccelerometerX = (double)refData.get("delta_accelerometer_x");
                        deltaAccelerometerY = (double)refData.get("delta_accelerometer_y");
                        deltaAccelerometerZ = (double)refData.get("delta_accelerometer_z");
                        deltaAmbientTemperature = (double)refData.get("delta_ambient_temperature");
                        deltaLight = (double)refData.get("delta_light");
                        deltaPressure = (double)refData.get("delta_pressure");
                        deltaRelativeHumidity = (double)refData.get("delta_relativeHumidity");
                        deltaProximity = (double)refData.get("delta_proximity");

                    } else {
                        Log.d("DB", "No such document");
                        Map<String, Object> dataRecord = DeltaValuesRegistry();
                        db.collection("sensores humanos").document("dispositivos moviles").collection("azul").document("capa azul").collection("users").document(mAuth.getCurrentUser().getEmail()).set(dataRecord)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("DB", "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("DB", "Error writing document", e);
                                    }
                                });
                        InitializeDeltas();
                    }
                } else {
                    Log.d("DB", "get failed with ", task.getException());
                }
            }
        });
    }

    private Map<String, Object> DeltaValuesRegistry(){
        Map<String, Object> dataRecord = new HashMap<>();
        dataRecord.put("delta_distance", 10.0D);
        dataRecord.put("delta_accelerometer_x", 10.0D);
        dataRecord.put("delta_accelerometer_y", 10.0D);
        dataRecord.put("delta_accelerometer_z", 10.0D);
        dataRecord.put("delta_ambient_temperature", 37.3D);
        dataRecord.put("delta_light", 4000.0D);
        dataRecord.put("delta_pressure", 110.0D);
        dataRecord.put("delta_relativeHumidity", 10.0D);
        dataRecord.put("delta_proximity", 1.0D);
        return dataRecord;
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
    protected void onDestroy() {
        super.onDestroy();
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