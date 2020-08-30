package com.example.opengldemo.GestureDetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.opengldemo.R;

public class GestureDetectorActivity extends AppCompatActivity {
    private final static String TAG = GestureDetectorActivity.class.getSimpleName();
    private GestureDetector mGestureDetector;
    private GestureDetector mSimpleGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_detector);
        ImageView imageView = findViewById(R.id.gesture_view);
        imageView.setImageResource(R.drawable.ic_launcher_foreground);

        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown: ");
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.d(TAG, "onShowPress: ");

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp: ");
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(TAG, "mGestureDetectoron#Scroll: distanceX: " + distanceX + " distanceY: " + distanceY);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: ");

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "mGestureDetector onFling: velocityX: " + velocityX + " velocityY: " + velocityY);
                return true;
            }
        });

        mSimpleGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.i(TAG, "onDoubleTap: ");
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                Log.d(TAG, "onDoubleTapEvent: ");
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "mSimpleGestureDetector: onFling: velocityX: " + velocityX + " velocityY: " + velocityY);
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(TAG, "mSimpleGestureDetector#onScroll: distanceX: " + distanceX + " distanceY: " + distanceY);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });

        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d(TAG, "onSingleTapConfirmed: ");
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "onDoubleTap: ");
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                Log.d(TAG, "onDoubleTapEvent: ");
                return true;
            }


        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mSimpleGestureDetector.onTouchEvent(event);
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });


    }
}
