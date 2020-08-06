package com.example.opengldemo.viewPage;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class MyViewPageAdapter extends PagerAdapter {
    private List<Integer> mImageList;

    MyViewPageAdapter(List<Integer> imageResources) {
        mImageList = imageResources;
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
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(container.getContext(), "第" + position + "个", Toast.LENGTH_SHORT).show();
            }
        });
        return imageButton;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);

    }
}
