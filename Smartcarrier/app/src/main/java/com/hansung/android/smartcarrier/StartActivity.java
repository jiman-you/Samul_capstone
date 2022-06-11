package com.hansung.android.smartlocker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button startbtn = findViewById(R.id.startbtn);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userPass = intent.getStringExtra("userPassword");
        String userName = intent.getStringExtra("userName");
        String userAge = intent.getStringExtra("userAge");
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("userID",userID);
                intent.putExtra("userPassword",userPass);
                intent.putExtra("userName",userName);
                intent.putExtra("userAge",userAge);
                startActivity(intent);
                finish();
            }
        });
    }
}
