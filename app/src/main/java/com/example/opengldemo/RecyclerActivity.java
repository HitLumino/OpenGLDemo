package com.example.opengldemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class RecyclerActivity extends AppCompatActivity {
    private List<Assets> assetsList = new ArrayList<>();
    private MyGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        intAsset();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        AssetAdapter assetAdapter = new AssetAdapter(assetsList);
        recyclerView.setAdapter(assetAdapter);

        RelativeLayout layout = findViewById(R.id.GL_surface);

        mGLSurfaceView = new MyGLSurfaceView(this);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this.mGLSurfaceView);

        layout.addView(mGLSurfaceView);

    }

    private void intAsset() {
        for (int i = 0; i < 20; i++) {
            String name = "羊驼" + i;
            Assets assets = new Assets(name, R.mipmap.ic_launcher_round);
            assetsList.add(assets);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}
