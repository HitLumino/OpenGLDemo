package com.example.opengldemo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private final static String TAG = CameraPreview.class.getSimpleName();

    private SurfaceHolder mSurfaceHolder;
    private int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            synchronized (this) {
                Log.i("frame", "good");
            }
        }
    };

    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);
        Log.d(TAG, "CameraPreview: ");
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //保持屏幕常亮
        mSurfaceHolder.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated: ");
        startCamera(mCameraIndex);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: ");
        stopCamera();
    }

    private void startCamera(int mCameraIndex) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(mCameraIndex);
            } catch (Exception e) {
                return;
            }
            Camera.Parameters params = mCamera.getParameters();
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                // 自动对焦
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            params.setPreviewFormat(ImageFormat.NV21); // 设置预览图片格式
            params.setPictureFormat(ImageFormat.JPEG); // 设置拍照图片格式
            mCamera.setParameters(params);

            mCamera.setPreviewCallback(previewCallback);
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                //旋转90度
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                mCamera.release();
                mCamera=null;
            }
        }

    }

    //关闭摄像机
    private void stopCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }

    }

    public static void followScreenOrientation(Context context, Camera camera){
        final int orientation = context.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(180);
        }else if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
        }
    }
}
