package com.hansung.android.smartlocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hansung.android.smartlocker.UI.GPSGetThingShadow;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    String urlStr;
    final static String TAG = "AndroidAPITest";
    public static Context mContext;
    Timer timer;
    GoogleMap mGoogleMap = null;
    LatLng location;
    public String LAT;
    public String LON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext =this;
        urlStr = "https://1mprllojea.execute-api.us-east-1.amazonaws.com/prod/devices/Samul";
        timer = new Timer();
        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               new GPSGetThingShadow(HomeActivity.this, urlStr).execute();
                           }
                       },
                0, 2000);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GridView menu = findViewById(R.id.homemenu);
        final String[] home = new String[]{"\n\n홈", "\n\n사용자제어", "\n\n일정", "\n\n무게"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.menu,
                home);
        menu.setAdapter(adapter);




        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userPass = intent.getStringExtra("userPassword");
        String userName = intent.getStringExtra("userName");
        String userAge = intent.getStringExtra("userAge");

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (position == 0) { // 홈버튼
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra("userID", userID);
                    intent.putExtra("userPassword", userPass);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userAge", userAge);
                    startActivity(intent);
                    finish();
                } else if (position == 1) {//사용자
                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                    intent.putExtra("userID", userID);
                    intent.putExtra("userPassword", userPass);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userAge", userAge);
                    startActivity(intent);
                    finish();
                } else if (position == 2) {//일정
                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    intent.putExtra("userID", userID);
                    intent.putExtra("userPassword", userPass);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userAge", userAge);
                    startActivity(intent);
                    finish();
                } else if (position == 3) {//무게
                    Intent intent = new Intent(getApplicationContext(), WeightActivity.class);
                    intent.putExtra("userID", userID);
                    intent.putExtra("userPassword", userPass);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userAge", userAge);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) { // 이전 프래그먼트에서 받아온 데이터에 해당하는 데이터베이스에 저장된 주소로 구글맵을 이동
        mGoogleMap = googleMap;
        double lat = Double.parseDouble(LAT);
        double lon = Double.parseDouble(LON);
        location = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions().position(location));
        // move the camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
    }


    }
