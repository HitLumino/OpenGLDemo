package com.example.opengldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = findViewById(R.id.surface_view);

        MySurfaceView mySurfaceView = new MySurfaceView(this);

        layout.addView(mySurfaceView);
    }
}
