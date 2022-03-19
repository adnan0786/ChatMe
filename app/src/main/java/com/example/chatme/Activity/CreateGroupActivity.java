package com.example.chatme.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatme.Fragment.GetGroupDetailFragment;
import com.example.chatme.R;

public class CreateGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().add(R.id.groupContainer,
                new GetGroupDetailFragment()).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}