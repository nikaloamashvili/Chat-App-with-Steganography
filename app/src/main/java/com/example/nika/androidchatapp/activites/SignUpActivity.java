package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.nika.androidchatapp.R;
import com.example.nika.androidchatapp.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setlistener();
    }

    private void setlistener(){
        binding.textSignIn.setOnClickListener(v->onBackPressed() );
    }

}