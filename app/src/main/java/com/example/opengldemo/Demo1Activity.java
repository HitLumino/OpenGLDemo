package com.example.opengldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.Log;

public class Demo1Activity extends AppCompatActivity {
    private final static String TAG = Demo1Activity.class.getSimpleName();
    private MyGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new MyGLSurfaceView(this);

        final ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(this.mGLSurfaceView);
        } else {
            Log.w(TAG, "not supprort gles20");
            return;
        }

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }
}
