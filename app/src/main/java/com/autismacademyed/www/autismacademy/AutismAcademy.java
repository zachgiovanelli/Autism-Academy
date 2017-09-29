package com.autismacademyed.www.autismacademy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class AutismAcademy extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autism_academy);
    }

    public void imageButtonOnClick(View v) {

        switch (v.getId()) {
            case R.id.imageButtonYellow:
                Intent browserIntent = new Intent(this, SecondActivity.class);
                startActivity(browserIntent);
                break;
            case R.id.imageButtonRed:
                Intent intent = new Intent(this, Blog.class);
                startActivity(intent);
                break;
        }

    }

}
