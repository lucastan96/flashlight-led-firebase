package com.lucastan96.flashlight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private Switch toggleSwitch, onlineSwitch;
    private TextView toggleView;
    private static final int CAMERA_REQUEST = 50;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("flashlight");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleSwitch = (Switch) findViewById(R.id.toggleSwitch);
        toggleSwitch.setEnabled(false);
        onlineSwitch = (Switch) findViewById(R.id.onlineSwitch);
        onlineSwitch.setChecked(false);
        toggleView = (TextView) findViewById(R.id.toggleView);

        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                if (!on) {
                    flashlightOff();
                } else {
                    flashlightOn();
                }
            }
        });

        onlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                if (!on) {
                    setOnline(0);
                    toggleView.setText("Flashlight cannot be remotely controlled at flashlight-193812.firebaseapp.com");
                } else {
                    setOnline(1);
                    toggleView.setText("Flashlight can be remotely controlled at flashlight-193812.firebaseapp.com");
                }
            }
        });
    }

    private void flashlightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            toggleSwitch.setChecked(true);
            setToggle();
        } catch (CameraAccessException e) {

        }
    }

    private void flashlightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            toggleSwitch.setChecked(false);
            setToggle();
        } catch (CameraAccessException e) {

        }
    }

    private void getData() {
        if (toggleSwitch.isEnabled()) {
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Integer toggle = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        toggle = snapshot.getValue(Integer.class);
                    }

                    if (toggle == 1) {
                        flashlightOn();
                    } else {
                        flashlightOff();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Firebase failed to load data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setToggle() {
        Integer toggle = 0;

        if (toggleSwitch.isChecked()) {
            toggle = 1;
        }

        myRef.child("toggle").setValue(toggle);
    }

    private void setOnline(Integer online) {
        myRef.child("online").setValue(online);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toggleSwitch.setEnabled(true);
                    getData();
                    Toast.makeText(MainActivity.this, "Yay! The app should now work properly.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Camera permissions must be enabled for app to function properly.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final boolean existFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        boolean permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if (existFlash) {
            if (permission) {
                toggleSwitch.setEnabled(true);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
            }
        } else {
            Toast.makeText(MainActivity.this, "No flash available on your device.", Toast.LENGTH_SHORT).show();
        }

        getData();
        setToggle();
        setOnline(0);
    }
}