package fr.chantapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import fr.chantapp.R;

public class CameraPermissionsActivity extends AppCompatActivity {

    public static final int CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_permissions);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
           checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(getApplicationContext(), TextDetectionActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_PERMISSION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), TextDetectionActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Permission non accordée", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}
