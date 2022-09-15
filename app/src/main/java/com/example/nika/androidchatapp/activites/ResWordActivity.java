package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.nika.androidchatapp.databinding.ActivityResWordBinding;
import com.example.nika.androidchatapp.databinding.ActivitySigninBinding;

public class ResWordActivity extends AppCompatActivity {
    private ActivityResWordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResWordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}