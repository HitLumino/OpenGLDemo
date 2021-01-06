package com.example.opengldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        LinearLayout layout = findViewById(R.id.surface_view);
//
//        MySurfaceView mySurfaceView = new MySurfaceView(this);
//
//        layout.addView(mySurfaceView);


        InputStream input = getResources().openRawResource(R.raw.json);
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(input, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String res = sb.toString();
        Log.e("zmm", "onCreate: " + sb.toString());

        String[] strings = res.split("\n");
        Log.e("zmm", "onCreate: "+ strings.length);

        // write json
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("exampleWrite.json"),"UTF-8");
            JSONObject obj=new JSONObject();
            obj.put("FLAG","1");
            for(Integer i=1;i<4;i++) {
                JSONObject subObj=new JSONObject();//创建对象数组里的子对象
                subObj.put("Name","array"+i);
                subObj.put("String","小白"+i);
                obj.accumulate("ARRAYS",subObj);
            }
            System.out.println(obj.toString());

            osw.write(obj.toString());
            osw.flush();//清空缓冲区，强制输出数据
            osw.close();//关闭输出流
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        readJson();
    }

    public void readJson() {
        //Json数据的读写
        try {
//            InputStream is = this.getAssets().open("test.json");//eclipse
            InputStream is = getClassLoader().getResourceAsStream("assets/" + "sample.json");//android studio
            BufferedReader bufr = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufr.readLine()) != null) {
                builder.append(line);
            }
            is.close();
            bufr.close();

            try {
                JSONObject root = new JSONObject(builder.toString());
                Log.d("info","cat=" + root.getString("cat"));
                JSONArray array = root.getJSONArray("languages");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject lan = array.getJSONObject(i);
                    Log.d("info","-----------------------");
                    Log.d("info","id=" + lan.getInt("id"));
                    Log.d("info","ide=" + lan.getString("ide"));
                    Log.d("info","name=" + lan.getString("name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Json数据的创建
        try {
            JSONObject root = new JSONObject();
            root.put("cat", "it");

            JSONObject lan1 = new JSONObject();
            lan1.put("id", 1);
            lan1.put("ide", "Eclipse");
            lan1.put("name", "Java");

            JSONObject lan2 = new JSONObject();
            lan2.put("id", 2);
            lan2.put("ide", "XCode");
            lan2.put("name", "Swift");

            JSONObject lan3 = new JSONObject();
            lan3.put("id", 3);
            lan3.put("ide", "Visual Studio");
            lan3.put("name", "C#");

            JSONArray array = new JSONArray();
            array.put(lan1);
            array.put(lan2);
            array.put(lan3);

            root.put("languages", array);

            Log.d("zmm",root.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
