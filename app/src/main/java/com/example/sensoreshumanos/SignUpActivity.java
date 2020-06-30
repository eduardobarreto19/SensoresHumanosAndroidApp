package com.example.sensoreshumanos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;


public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button signupButton;
    private EditText email;
    private EditText password;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signupButton = findViewById(R.id.signup_button);
        email = (EditText)findViewById(R.id.editTextEmailAddress);
        password = (EditText)findViewById(R.id.editTextPassword);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Map<String, Object> dataRecord = DeltaValuesRegistry();
                                db.collection("users").document(mAuth.getCurrentUser().getEmail()).set(dataRecord)
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
                                startActivity(new Intent(SignUpActivity.this, DataGeneratorActivity.class));
                                finish();
                            }
                        }
                    });
            }
        });
    }

    private Map<String, Object> DeltaValuesRegistry(){
        Map<String, Object> dataRecord = new HashMap<>();
        dataRecord.put("deltaLat", 0.0005f);
        dataRecord.put("deltaLong", 0.0005f);
        dataRecord.put("deltaAltitude", 10.0f);
        dataRecord.put("deltaDistance", 10.0f);
        dataRecord.put("deltaAccelerometerX", 10.0f);
        dataRecord.put("deltaAccelerometerY", 10.0f);
        dataRecord.put("deltaAccelerometerZ", 10.0f);
        dataRecord.put("deltaAmbientTemperature", 37.3f);
        dataRecord.put("deltaLight", 4000.0f);
        dataRecord.put("deltaPressure", 110.0f);
        dataRecord.put("deltaRelativeHumidity", 10.0f);
        dataRecord.put("deltaProximity", 1.0f);
        return dataRecord;
    }
}