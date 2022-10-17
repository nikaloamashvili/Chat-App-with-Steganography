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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nika.androidchatapp.R;
import com.example.nika.androidchatapp.databinding.ActivityResAudioBinding;
import com.example.nika.androidchatapp.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ResAudioActivity extends AppCompatActivity {
    private ActivityResAudioBinding binding;
    private ImageView imagePlayPause;
    private TextView TextCurrentTime, textTotalDuration;
    private SeekBar playerSeekBar;
    private MediaPlayer mediaPlayer;
    private Handler handler=new Handler();
    String value = null;
    String urll;
    private FirebaseFirestore database;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityResAudioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle extras = getIntent().getExtras();
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
                        mediaPlayer=new MediaPlayer();
                        binding.playerSeekBar.setMax(100);
                        binding.imagePlayPause.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mediaPlayer.isPlaying()){
                                    handler.removeCallbacks(updater);
                                    mediaPlayer.pause();
                                    binding.imagePlayPause.setImageResource(R.drawable.ic_play);
                                }else {
                                    mediaPlayer.start();
                                    binding.imagePlayPause.setImageResource(R.drawable.ic_pause);
                                    updateSeekBar();
                                }
                            }
                        });

                        prepareMediaPlayer();
                        binding.playerSeekBar.setOnTouchListener(new View.OnTouchListener() {
                            @SuppressLint("ClickableViewAccessibility")
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                SeekBar seekBar=(SeekBar) view;
                                int playPosition=(mediaPlayer.getDuration()/100)*seekBar.getProgress();
                                mediaPlayer.seekTo(playPosition);
                                binding.TextCurrentTime.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));

                                return false;
                            }
                        });

                        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                            @Override
                            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                                binding.playerSeekBar.setSecondaryProgress(i);

                            }
                        });

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                binding.playerSeekBar.setProgress(0);
                                binding.imagePlayPause.setImageResource(R.drawable.ic_play);
                                binding.TextCurrentTime.setText(R.string.zero);
                                binding.textTotalDuration.setText(R.string.zero);
                                mediaPlayer.reset();
                                prepareMediaPlayer();
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

    @Override
    public void onBackPressed() {
        if (mediaPlayer.isPlaying()){
            handler.removeCallbacks(updater);
            mediaPlayer.pause();
            binding.imagePlayPause.setImageResource(R.drawable.ic_play);
        }
            super.onBackPressed();
    }

    private void prepareMediaPlayer(){
        try {
            mediaPlayer.setDataSource(urll);
            Log.d("tag11", urll);

            mediaPlayer.prepare();
            binding.textTotalDuration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

        }catch (Exception exception){
            Toast.makeText(this,exception.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long correntDuration=mediaPlayer.getCurrentPosition();
            binding.TextCurrentTime.setText(milliSecondsToTimer(correntDuration));
        }
    };

    private void updateSeekBar(){
        if (mediaPlayer.isPlaying()){
            binding.playerSeekBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration()) *100));
            handler.postDelayed(updater,1000);
        }
    }

    private String milliSecondsToTimer(long milliseconds){
        String timerString="";
        String secondsString;

        int hours=(int)(milliseconds/(100*60*60));
        int minutes=(int)(milliseconds % (1000*60*60))/ (1000*60);
        int seconds=(int) ((milliseconds%1000*60*60)%(1000*60)/1000);

        if (hours>0){
            timerString=hours+":";
        }
        if (seconds<10){
            secondsString="0"+seconds;
        }else {
            secondsString=""+seconds;
        }
        timerString=timerString+minutes+":"+secondsString;
        return timerString;


    }
}