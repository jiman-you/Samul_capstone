package com.hansung.android.smartlocker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CalendarActivity extends AppCompatActivity {
    String userID;
    String userPass;
    String userName;
    String userAge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendaractivity);
        Intent intent = getIntent();
         userID = intent.getStringExtra("userID");
         userPass = intent.getStringExtra("userPassword");
         userName = intent.getStringExtra("userName");
         userAge = intent.getStringExtra("userAge");


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container, new MonthViewFragment());
        fragmentTransaction.commit();
    }
}