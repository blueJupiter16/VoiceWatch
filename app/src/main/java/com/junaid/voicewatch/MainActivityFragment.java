package com.junaid.voicewatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivityFragment extends Fragment{

    public static final long REFRESH_RATE = 0;
    private static final String TAG = "Speech Output" ;

    private TextView mView;
    private Button mButton,mReset;
    private long startTime,timeInMillis,updatedTime,timeSwapBuff;
    private int secs,mins,millisecs;
    int controller = 0;
    private View view;
    private Intent mRecognizerIntent;
    private SpeechRecognizer mSpeech = null;
    private Handler mHandler = new Handler();
    private int mCount= 0;

    private BroadcastReceiver mBroadcastReceiver = null;

/*
    private SpeechRecognizer getSpeechRecognizer(){
        if (mSpeech == null) {
            mSpeech = SpeechRecognizer.createSpeechRecognizer(getActivity());
            mSpeech.setRecognitionListener(this);
        }

        return mSpeech;
    }

    public void startVoiceRecognitionCycle()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getActivity().getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        getSpeechRecognizer().startListening(intent);
        mCount++;
        Log.d("Cycle",""+mCount);

    }

*/


    /*------------------------------ Fragment Methods-------------------------------------------*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*        mSpeech = SpeechRecognizer.createSpeechRecognizer(getActivity());
        mSpeech.setRecognitionListener(this);
        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getActivity().getPackageName());
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

       // Toast.makeText(getActivity(),"onCreate",Toast.LENGTH_SHORT).show();
        mSpeech.startListening(mRecognizerIntent);
        Log.d("Listener",mSpeech.toString());*/

        //startVoiceRecognitionCycle();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);



       view = inflater.inflate(R.layout.fragment_display,container,false);
        //inflating the view

        mView = (TextView) view.findViewById(R.id.display_time_id);
        mButton = (Button) view.findViewById(R.id.control_button);
        mReset = (Button) view.findViewById(R.id.reset_button);
        mReset.setVisibility(view.GONE);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Pressing start

                if(controller == 0) {

                    start();
                }

                //Pressing pause
                else{

                    pause();
                }
            }
        });

        mReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            reset();

            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

             //   Log.d("Broadcast","Broadcast Received");

                String s = intent.getStringExtra(VoiceListeningService.BROADCAST_STRING);
                if(s != null){
                    Log.d("Broadcast",s);
                    Toast.makeText(getActivity(),"' " + s + " ' ",Toast.LENGTH_SHORT).show();

                    if(s.matches("start") || s.matches("begin"))
                        start();
                    if(s.matches("stop") || s.matches("pause")){
                        pause();
                    }
                    if(s.matches("restart") || s.matches("reset")){
                        reset();
                    }

                }

            }
        };

        return view;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mBroadcastReceiver,new IntentFilter(VoiceListeningService.FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            timeInMillis = System.currentTimeMillis() - startTime;
            updatedTime =  timeSwapBuff + timeInMillis;
            secs = (int) (updatedTime / 1000);
            mins =  secs/60;
            millisecs = (int) updatedTime % 1000;

            int newMils = millisecs/10;

            mView.setText("" + String.format("%02d",mins) +":"+String.format("%02d",secs)+":"+ String.format("%02d",newMils));
            mHandler.postDelayed(this,REFRESH_RATE);


        }
    };
    // Stop watch Functions
    private void reset(){
        startTime = 0;
        timeInMillis=0;
        timeSwapBuff=0;
        controller = 0;
        secs=0;
        mins=0;
        millisecs=0;
        mButton.setText("Start");
        mHandler.removeCallbacks(updateTimer);
        mView.setText("00:00:00");

        mReset.setVisibility(view.GONE);
    }


    private void start(){


        startTime = System.currentTimeMillis();
        mHandler.postDelayed(updateTimer, REFRESH_RATE);
        mButton.setText("Pause");
        controller = 1;

    }

    private void pause(){
        timeSwapBuff+=timeInMillis;
        mHandler.removeCallbacks(updateTimer);
        mButton.setText("Start");
        mReset.setVisibility(view.VISIBLE);
        controller=0;
    }


    /*---------------------- Voice Control Methods ------------------------*/
   /* @Override
    public void onReadyForSpeech(Bundle params) {
        //Toast.makeText(getActivity(),"Ready for Speech",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {
        Toast.makeText(getActivity(),"Beginning Speech",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Toast.makeText(getActivity(),"End of Speech",Toast.LENGTH_SHORT).show();
       // mSpeech.startListening(mRecognizerIntent);
       // startVoiceRecognitionCycle();
        mSpeech.destroy();
    }

    @Override
    public void onError(int error) {

        String message;
        boolean restart = true;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                restart = false;
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                restart = false;
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Not recognised";
                break;

        }

        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
        /*if (restart) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    getSpeechRecognizer().cancel();
                    startVoiceRecognitionCycle();
                }
            });
        }*/
    /*

    }

    @Override
    public void onResults(Bundle results) {

        startVoiceRecognitionCycle();

       // StringBuilder scores = new StringBuilder();
        voiceAction(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));

        Log.d(TAG,"onResults: " + results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));

        // Return to the container activity dictation results



    }

    @Override
    public void onPartialResults(Bundle partialResults) {

       // voiceAction(partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }*/



    /*----------------Other helper Functions -------------------*/
    private void voiceAction(String s){
        if(s.matches("start"))
            start();
        else if(s.matches("stop"))
            pause();
        else if(s.matches("reset"))
            reset();
    }
}
