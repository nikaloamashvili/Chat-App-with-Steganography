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

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;



import com.example.nika.androidchatapp.databinding.ActivityChangePasswordBinding;
import com.example.nika.androidchatapp.databinding.ActivityForgetPasswordBinding;
import com.example.nika.androidchatapp.databinding.ActivityResImageBinding;
import com.example.nika.androidchatapp.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ResImageActivity extends AppCompatActivity {
    private ActivityResImageBinding binding;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;
    private FirebaseFirestore database;
    private OkHttpClient okHttpClient;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.imageView.setImageBitmap(null);

        Bundle extras = getIntent().getExtras();
        String value = null;
        if(extras !=null) {
             value = extras.getString("KEY");
        }
        Log.d("ttt", value);
        Log.d("ttt", "fsdfsd");
        database =FirebaseFirestore.getInstance();


        DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_CHAT).document(value.toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        okHttpClient = new OkHttpClient();

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
                        new FetchImage(document.getString(Constants.KEY_URL)).start();

                    } else {
                    }
                } else {
                }
            }
        });




    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    class FetchImage extends Thread{

        String URL;
        Bitmap bitmap;

        FetchImage(String URL){

            this.URL = URL;

        }

        @Override
        public void run() {

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    progressDialog = new ProgressDialog(ResImageActivity.this);
                    progressDialog.setMessage("Getting your pic....");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });

            InputStream inputStream = null;
            try {
                inputStream = new URL(URL).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    binding.imageView.setImageBitmap(bitmap);

                }
            });




        }
    }

    private void showToast (String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

}