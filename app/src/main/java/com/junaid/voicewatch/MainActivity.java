package com.junaid.voicewatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.Voice;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startService();


        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        Fragment fragment = new MainActivityFragment();
        fragmentTransaction.add(R.id.activity_main,fragment,"STOPWATCH");
        fragmentTransaction.commit();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startService();

    }


    private void startService(){
        intent = new Intent(this, VoiceListeningService.class);
        startService(intent);
    }

    /*
    private class StartListeningService extends AsyncTask<Context,Void,Void>{



        @Override
        protected Void doInBackground(Context... params) {
            if(params.length != 0){

                Intent intent = new Intent(params[0], VoiceListeningService.class);
                startService(intent);
                Log.d("Service","Starting Service.....");
            }

            return null;
        }
    }*/





}
