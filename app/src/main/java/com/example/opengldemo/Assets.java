package com.example.opengldemo;

public class Assets {
    private String mName;
    private int mImageId;

    public Assets(String name, int imageId) {
        mName = name;
        mImageId = imageId;
    }

    public int getImageId() {
        return mImageId;
    }

    public String getName() {
        return mName;
    }
}
