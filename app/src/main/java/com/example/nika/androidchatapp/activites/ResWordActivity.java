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

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.nika.androidchatapp.R;
import com.example.nika.androidchatapp.databinding.ActivityResWordBinding;
import com.example.nika.androidchatapp.databinding.ActivitySigninBinding;
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

public class ResWordActivity extends AppCompatActivity {
    private ActivityResWordBinding binding;
    String urll;
    private FirebaseFirestore database;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResWordBinding.inflate(getLayoutInflater());
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
                        String UrlofFile;
                        String TypeOfFile;
                        Log.d("tag22", urll.toString());
                        //Toast.makeText(l.getContext(), getMimeType(chatMessages.get(position).message.toString()),Toast.LENGTH_SHORT).show();
                        //Toast.makeText(l.getContext(), Arrays.toString((chatMessages.get(position).message.toString()).split(".")),Toast.LENGTH_SHORT).show();
                        List<String> arrOfStr = Arrays.asList(urll.split("\\."));
                        //Toast.makeText(l.getContext(),chatMessages.get(position).message.toString() ,Toast.LENGTH_SHORT).show();

                        TypeOfFile= arrOfStr.get(arrOfStr.size() - 1);
                        UrlofFile=urll.toString().substring(0, urll.toString().length() -(TypeOfFile.length()+1));
                        DownloadManager.Request request1=new DownloadManager.Request(Uri.parse(urll.toString()));
                        String tempTitle="check";
                        request1.setTitle(tempTitle);
                        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB){
                            request1.allowScanningByMediaScanner();
                            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }

                        request1.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"new word file"+"."+"doc");
                        DownloadManager downloadManager=(DownloadManager) getBaseContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        request1.setMimeType(null);
                        request1.allowScanningByMediaScanner();
                        request1.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                        downloadManager.enqueue(request1);
                        Toast.makeText(getBaseContext(),"downloading..." ,Toast.LENGTH_SHORT).show();





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