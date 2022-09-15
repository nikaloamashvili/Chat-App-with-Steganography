package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.nika.androidchatapp.databinding.ActivityEncodeBinding;
import com.example.nika.androidchatapp.databinding.ActivityUsersBinding;

public class EncodeActivity extends AppCompatActivity {
    private ActivityEncodeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityEncodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}