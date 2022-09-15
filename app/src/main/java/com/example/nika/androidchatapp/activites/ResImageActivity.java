package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import com.example.nika.androidchatapp.databinding.ActivityChangePasswordBinding;
import com.example.nika.androidchatapp.databinding.ActivityForgetPasswordBinding;
import com.example.nika.androidchatapp.databinding.ActivityResImageBinding;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ResImageActivity extends AppCompatActivity {
    private ActivityResImageBinding binding;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;


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
        new FetchImage(value).start();


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

}