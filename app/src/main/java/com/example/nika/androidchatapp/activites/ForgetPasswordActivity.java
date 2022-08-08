package com.example.nika.androidchatapp.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.example.nika.androidchatapp.databinding.ActivityChangePasswordBinding;
import com.example.nika.androidchatapp.databinding.ActivityForgetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivityForgetPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

    }

    private void showToast (String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private  void setListeners(){
        binding.buttonResetpassword.setOnClickListener(v ->
        {
            if(binding.inputEmail.getText().toString().trim().isEmpty()){
                showToast("Enter email");
            }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
                showToast("Enter valid email");
            }else{
                resetPassword();
            }


    });}

    private  void resetPassword(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.sendPasswordResetEmail(binding.inputEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showToast("Check your inbox to reset the password to temp password");
                startActivity(new Intent(getApplicationContext(),ChangePasswordActivity.class));

            }
        });

    }



}