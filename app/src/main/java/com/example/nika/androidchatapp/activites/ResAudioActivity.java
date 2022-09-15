package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.nika.androidchatapp.databinding.ActivityResAudioBinding;
import com.example.nika.androidchatapp.databinding.ActivityResImageBinding;
import com.example.nika.androidchatapp.databinding.ActivityResMp4Binding;

public class ResAudioActivity extends AppCompatActivity {
    private ActivityResAudioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityResAudioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle extras = getIntent().getExtras();
        String value = null;
        if(extras !=null) {
            value = extras.getString("KEY");
        }

    }
}