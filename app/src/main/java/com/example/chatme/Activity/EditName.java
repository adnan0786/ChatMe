package com.example.chatme.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatme.Constants.AllConstants;
import com.example.chatme.Utils.Util;
import com.example.chatme.databinding.ActivityEditNameBinding;

public class EditName extends AppCompatActivity {

    private ActivityEditNameBinding binding;
    private String fName, lName;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNameBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        util = new Util();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra("name")) {
            String name = getIntent().getStringExtra("name");
            if (name.contains(" ")) {
                String[] split = name.split(" ");
                binding.edtFName.setText(split[0]);
                binding.edtLName.setText(split[1]);

            } else {
                binding.edtFName.setText(name);
                binding.edtLName.setText("");
            }
        }

        binding.btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFName() & checkLName()) {
                    Intent intent = new Intent();
                    intent.putExtra("name", fName + " " + lName);
                    setResult(AllConstants.USERNAME_CODE, intent);
                    finish();
                }
            }
        });
    }

    private boolean checkFName() {
        fName = binding.edtFName.getText().toString().trim();
        if (fName.isEmpty()) {
            binding.edtFName.setError("Field is required");
            return false;
        } else {
            binding.edtFName.setError(null);
            return true;
        }
    }

    private boolean checkLName() {
        lName = binding.edtLName.getText().toString().trim();
        if (fName.isEmpty()) {
            binding.edtLName.setError("Field is required");
            return false;
        } else {
            binding.edtLName.setError(null);
            return true;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        util.updateOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }


}