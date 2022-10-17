package com.example.nika.androidchatapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.nika.androidchatapp.adapters.UsersAdapter;
import com.example.nika.androidchatapp.databinding.ActivityUsersBinding;
import com.example.nika.androidchatapp.listeners.UserListener;
import com.example.nika.androidchatapp.models.User;
import com.example.nika.androidchatapp.utilities.Constants;
import com.example.nika.androidchatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private  void  setListeners(){
        binding.imageBack.setOnClickListener(v->onBackPressed());
    }

    private  void getUsers(){
        loading(true);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.Key_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.Key_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null ){
                        List<User> users =new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name =queryDocumentSnapshot.getString(Constants.Key_NAME);
                            user.email =queryDocumentSnapshot.getString(Constants.Key_EMAIL);
                            user.image =queryDocumentSnapshot.getString(Constants.Key_IMAGE);
                            user.token =queryDocumentSnapshot.getString(Constants.key_fcm_token);
                            user.id =queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size()>0){
                            UsersAdapter usersAdapter =new UsersAdapter(users,this);
                            binding.userRecyclerView.setAdapter(usersAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }

                });

    }

    private  void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);

    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}