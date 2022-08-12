package com.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.demo.R;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), HttpActivity.class));
            finish();
        }, 666);
    }

}