package com.example.nika.androidchatapp.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.nika.androidchatapp.databinding.ActivityResMp4Binding;
import com.example.nika.androidchatapp.databinding.ActivityResWordBinding;
import com.example.nika.androidchatapp.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ResMp4Activity extends AppCompatActivity {
    private ActivityResMp4Binding binding;
    String urll;
    private FirebaseFirestore database;
    private OkHttpClient okHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityResMp4Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle extras = getIntent().getExtras();
        String value = null;
        if(extras !=null) {
            value = extras.getString("KEY");
        }
        database = FirebaseFirestore.getInstance();


        DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_CHAT).document(value.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        urll=document.getString(Constants.KEY_URL);
                        binding.videoView.setVideoPath(urll.toString());
                        binding.videoView.start();
                        okHttpClient = new OkHttpClient();
//                                .newBuilder()
//                                .connectTimeout(60, TimeUnit.SECONDS)
//                                .writeTimeout(60, TimeUnit.SECONDS)
//                                .readTimeout(60, TimeUnit.SECONDS)
//                                .build();
                        // dummyText with a name 'sample'
                        RequestBody formbody
                                = new FormBody.Builder()
                                .add("sample0", "decode")
                                .add("sample", document.getString(Constants.KEY_IDD))
                                .add("sample1", "textuser")
                                .build();

                        // while building request
                        // we give our form
                        // as a parameter to post()
                        Request request = new Request.Builder().url("https://textofnika.azurewebsites.net/debug")
                                .post(formbody)
                                .build();
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(
                                    @NotNull Call call,
                                    @NotNull IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "server down", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                                showToast(response.body().string());
                                String responseData = response.body().string();
                                if (responseData!=null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String x=responseData.toString();
                                            Toast.makeText(getApplicationContext(),x , Toast.LENGTH_LONG).show();

                                        }
                                    });
                                }
                            }
                        });





                    } else {
                        Log.d("ttt", "No such document");
                    }
                } else {
                    Log.d("ttt", "get failed with ", task.getException());
                }
            }
        });


    }
}