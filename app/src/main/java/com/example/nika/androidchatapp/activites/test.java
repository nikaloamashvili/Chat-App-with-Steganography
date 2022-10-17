package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.nika.androidchatapp.databinding.ActivityChatBinding;
import com.example.nika.androidchatapp.databinding.ActivityTest2Binding;
import com.google.firebase.database.annotations.NotNull;

import java.io.IOException;

public class test extends AppCompatActivity {

    private ActivityTest2Binding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityTest2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // creating a client
        OkHttpClient okHttpClient = new OkHttpClient();

        // building a request
        Request request = new Request.Builder().url("https://textofnika.azurewebsites.net/").build();

        // making call asynchronously
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            // called if server is unreachable
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(test.this, "server down", Toast.LENGTH_SHORT).show();
                        binding.pagename.setText("error connecting to the server");
                    }
                });
            }

            @Override
            // called if we get a
            // response from the server
            public void onResponse(
                    @NotNull Call call,
                    @NotNull Response response)
                    throws IOException {  binding.pagename.setText(response.body().string());
            }
        });
    }
    }
