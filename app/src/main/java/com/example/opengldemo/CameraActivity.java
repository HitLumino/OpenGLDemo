package com.example.opengldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CameraActivity extends AppCompatActivity {
    private final static String TAG = CameraActivity.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private SurfaceHolder surfaceHolder;
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            /**
             * 只有在SurfaceView生命周期方法-->SurfaceView被创建后在打开相机
             * 以前我在 onResume 之前去打开相机，结果报错了，所以只有在这里打开相机，才是安全🔐
             */
            // Camera要选择hardware.Camera，因为Camera属于硬件hardware
            // Camera.open(1); // 这了传入的值，可以指定为：前置摄像头/后置摄像头
            mCamera = getCameraInstance();

            /**
             * 设置Camera与SurfaceHolder关联，Camera的数据让SurfaceView显示
             */
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "相机设置预览失败");
            }

            /**
             * 开始显示
             */
            mCamera.startPreview();

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            /**
             * SurfaceView被销毁后，一定要释放硬件资源，Camera是硬件
             */
            mCamera.stopPreview(); // (一定要有，不然只release也可能出问题)
            mCamera.release();
            mCamera = null;
            System.gc();

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }

        if (!checkCameraHardware(this)) {
            return;
        }



        SurfaceView surfaceView = findViewById(R.id.camera_view);
        // 不能直接操作SurfaceView，需要通过SurfaceView拿到SurfaceHolder
        surfaceHolder = surfaceView.getHolder();
        // 使用SurfaceHolder设置屏幕高亮，注意：所有的View都可以设置 设置屏幕高亮
        surfaceHolder.setKeepScreenOn(true);
        // 使用SurfaceHolder设置把画面或缓存 直接显示出来
       // surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(callback);


//        mCamera = getCameraInstance();
//        mCameraPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = findViewById(R.id.camera_surface_view2);
//        preview.addView(mCameraPreview);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.e(TAG, "getCameraInstance: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ">>>> onDestroy()");
        if (null != callback) surfaceHolder.removeCallback(callback);
    }


}
