package com.example.nika.androidchatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nika.androidchatapp.R;
import com.example.nika.androidchatapp.databinding.ActivityResAudioBinding;

public class ResAudioActivity extends AppCompatActivity {
    private ActivityResAudioBinding binding;
    private ImageView imagePlayPause;
    private TextView TextCurrentTime, textTotalDuration;
    private SeekBar playerSeekBar;
    private MediaPlayer mediaPlayer;
    private Handler handler=new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityResAudioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle extras = getIntent().getExtras();
        String value = null;
        if(extras !=null) {
            value = extras.getString("KEY");
        }
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

    }

    private void prepareMediaPlayer(){
        try {
            mediaPlayer.setDataSource("URL");
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
