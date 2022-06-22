package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.SyncStateContract;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.nika.androidchatapp.R;
import com.example.nika.androidchatapp.databinding.ActivitySigninBinding;
import com.example.nika.androidchatapp.utilities.Constants;
import com.example.nika.androidchatapp.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SigninActivity extends AppCompatActivity {

    private ActivitySigninBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager=new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.Key_IS_SIGN_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        //nika@gmail.com 123-9Hh
        binding =ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

    }

    private  void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.buttonSignin.setOnClickListener(v->{
            if(isValidSignInDetails()){
                signIn();
            }
        });
    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.Key_COLLECTION_USERS)
                .whereEqualTo(Constants.Key_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.Key_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()&& task.getResult()!=null&&
                            task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.Key_IS_SIGN_IN,true);
                        preferenceManager.putString(Constants.Key_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.Key_NAME,documentSnapshot.getString(Constants.Key_NAME));
                        preferenceManager.putString(Constants.Key_IMAGE,documentSnapshot.getString(Constants.Key_IMAGE));
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
    }
    private void loading(boolean isLoading){
        if (isLoading){
            binding.buttonSignin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignin.setVisibility(View.VISIBLE);
        }
    }
    private void showToast (String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }else {
            return true;
        }
    }
}