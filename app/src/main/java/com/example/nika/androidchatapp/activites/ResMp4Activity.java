package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.nika.androidchatapp.databinding.ActivityResMp4Binding;
import com.example.nika.androidchatapp.databinding.ActivityResWordBinding;

public class ResMp4Activity extends AppCompatActivity {
    private ActivityResMp4Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityResMp4Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}