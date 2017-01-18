package com.junaid.voicewatch;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.RecognizerResultsIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Junaid on 13-01-2017.
 */

public class VoiceListeningService extends Service {

    private static final String DEBUG_STRING = "SERVICE_DEBUG";

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static final int MSG_RECOGNIZER_LISTEN_AGAIN = 3;

    public static final String BROADCAST_STRING = "broadcast_string";
    public static final String FILTER = "filter_intent";

    private Intent mSpeechRecognizerIntent;
    private SpeechRecognizer mSpeechRecognizer = null;
    protected boolean mIsListening = false;
    protected boolean mResults = false;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    protected void createRecognizer(){
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createRecognizer();

        Message message = Message.obtain(null,MSG_RECOGNIZER_START_LISTENING);
        try {
            mServerMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        //Toast.makeText(getApplicationContext(),"inside on Create",Toast.LENGTH_SHORT).show();
    }

    protected static class IncomingHandler extends Handler{

        private WeakReference<VoiceListeningService> mTarget;

        IncomingHandler(VoiceListeningService target){
            mTarget = new WeakReference<>(target);
            Log.d(DEBUG_STRING,"inside handler");
        }

        @Override
        public void handleMessage(Message msg) {

            final VoiceListeningService target = mTarget.get();

            Log.d(DEBUG_STRING,"message sent: "+ msg.what);

            switch(msg.what){

                case MSG_RECOGNIZER_START_LISTENING:
                    if (!target.mIsListening)
                    {
                        Log.d(DEBUG_STRING,"Inside case MSG_START_LISTENING");
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        //Log.d(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    target.mSpeechRecognizer.destroy();
                    target.mIsListening = false;
                    Log.d(DEBUG_STRING, "message canceled"); //$NON-NLS-1$
                    target.onDestroy();
                    break;
                case MSG_RECOGNIZER_LISTEN_AGAIN:

                    target.createRecognizer();
                    target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                    target.mIsListening = true;
                    break;



            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    protected class SpeechRecognitionListener implements RecognitionListener{

        @Override
        public void onReadyForSpeech(Bundle params) {

           // Toast.makeText(getBaseContext(), "Started From Service !", Toast.LENGTH_SHORT).show();
            Log.v("DEBUG","onReadyForSpeech");
            mIsListening = true;
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

            if (!mResults) {

                restartListener();

                //onResults(null);

            }
        }

        @Override
        public void onError(int error) {

            String message = null;
            boolean restart = false;
            Message handlerMessage;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";
                    //restart = false;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";
                   // restart = false;
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
                    cancelListener();
                    restart = true;
                    break;
                default:
                    message = "Not recognised";
                    break;

            }

            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            if(!restart)
                handlerMessage = Message.obtain(null,MSG_RECOGNIZER_CANCEL);
            else
                handlerMessage = Message.obtain(null,MSG_RECOGNIZER_LISTEN_AGAIN);

            try {
                mServerMessenger.send(handlerMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onResults(Bundle results) {

          //  Toast.makeText(getApplicationContext(),"onResults",Toast.LENGTH_SHORT).show();
           // Toast.makeText(getApplicationContext(),results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0),
             //       Toast.LENGTH_SHORT).show();
           // Log.d(DEBUG_STRING,results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
            mResults = false;
            ArrayList<String> strings = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(strings == null){

            }

            Log.d(DEBUG_STRING,"{ " + strings.get(0));
            Intent intent = new Intent(FILTER);
            intent.putExtra(BROADCAST_STRING,strings.get(0));
            sendBroadcast(intent);
            restartListener();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

           // //Toast.makeText(getApplicationContext(),partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0),
                //    Toast.LENGTH_SHORT).show();
            mResults = true;
            int i= 0;
            ArrayList<String> results = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
           // Toast.makeText(getApplicationContext(),results.get(0),Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_STRING,"{ " + results.get(0));
            Intent intent = new Intent(FILTER);
            intent.putExtra(BROADCAST_STRING,results.get(0));
            sendBroadcast(intent);


        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }


    protected void cancelListener(){

        mSpeechRecognizer.destroy();
        mSpeechRecognizer = null;
        mSpeechRecognizerIntent = null;
        mIsListening = false;

    }

    protected void restartListener(){
        cancelListener();
        //Toast.makeText(getApplicationContext(),"End of Speech !",Toast.LENGTH_SHORT).show();
        Message message = Message.obtain(null,MSG_RECOGNIZER_LISTEN_AGAIN);


        try {
            mServerMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
