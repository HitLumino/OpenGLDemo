package com.example.opengldemo.viewPage;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.PagerAdapter;

import com.example.opengldemo.CameraPreview;
import com.example.opengldemo.MyGLSurfaceView;
import com.example.opengldemo.MySurfaceView;
import com.example.opengldemo.R;

import java.util.List;

public class MyViewPageAdapter extends PagerAdapter {
    private List<Integer> mImageList;
    private Context mContext;

    MyViewPageAdapter(List<Integer> imageResources, Context context) {
        mImageList = imageResources;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        ImageButton imageButton = new ImageButton(container.getContext());
        imageButton.setImageResource(mImageList.get(position));
        imageButton.setAdjustViewBounds(true);
        container.addView(imageButton);
        final LinearLayout layout = ((Activity)mContext).findViewById(R.id.camera_layout);
        final MyGLSurfaceView myGLSurfaceView = new MyGLSurfaceView(mContext);
        myGLSurfaceView.setEGLContextClientVersion(2);
        myGLSurfaceView.setRenderer(myGLSurfaceView);
        final CameraPreview cameraPreview = new CameraPreview(mContext);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(container.getContext(), "第" + position + "个", Toast.LENGTH_SHORT).show();

                layout.removeAllViews();
                if (position % 2 == 0) {
                    layout.addView(myGLSurfaceView);

                } else {
                    layout.removeAllViews();
                    layout.addView(cameraPreview);
                }

            }
        });
        return imageButton;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
