package com.example.opengldemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private DrawThread mDrawThread;

    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawThread = new DrawThread(holder.getSurface());
        mDrawThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawThread.stopDraw();
        try {
            mDrawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static class DrawThread extends Thread {
        private Paint mPaint = new Paint();
        private Surface mSurface;
        private boolean isRunning = true;


        DrawThread(Surface surface) {
            mSurface = surface;
            mPaint.setColor(Color.RED);
        }

        void stopDraw() {
            isRunning = false;
        }

        @Override
        public void run() {
            super.run();
            while (isRunning) {
                Canvas canvas = mSurface.lockCanvas(null);
                canvas.drawColor(Color.WHITE);
                canvas.drawRect(0, 0, 500, 500, mPaint);
                canvas.drawText("中", 300, 1000, mPaint);
                canvas.drawText("国", 300, 1500, mPaint);

                mPaint.setTextSize(500);
                mSurface.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
