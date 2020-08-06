package com.example.opengldemo.viewPage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.opengldemo.CameraPreview;
import com.example.opengldemo.R;

import java.util.ArrayList;
import java.util.List;

public class ViewPageActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private CameraPreview cameraPreview;
    private List<Integer> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);
        viewPager = findViewById(R.id.view_pager);
        imageList.add(R.drawable.ic_launcher_background);
        imageList.add(R.mipmap.ic_launcher);
        imageList.add(R.mipmap.ic_launcher_round);
        imageList.add(R.drawable.ic_launcher_foreground);
        viewPager.setAdapter(new MyViewPageAdapter(imageList));

        LinearLayout layout = findViewById(R.id.camera_layout);
        cameraPreview = new CameraPreview(this);
        layout.addView(cameraPreview);

    }
}
