package com.example.research_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView State;
    TextView Result;
    TextView TakeResult;

    TextView Flag;
    TextView FlagResult1;
    TextView FlagResult2;
    TextView FlagResult3;

    private int count = 0;
    private final Context context = this;
    private final Handler handler = new Handler();
    private final Runnable Task = new Runnable() {
        @Override
        public void run() {
            startSpeechRecognition();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        State = (TextView)findViewById(R.id.State);
        Result = (TextView)findViewById(R.id.Result);
        TakeResult = (TextView)findViewById(R.id.TakeResult);

        Flag = (TextView)findViewById(R.id.Flag);
        FlagResult1 = (TextView)findViewById(R.id.ResultState1);
        FlagResult2 = (TextView)findViewById(R.id.ResultState2);
        FlagResult3 = (TextView)findViewById(R.id.ResultState3);

        handler.postDelayed(Task, 1000);

    }

    private static String TAG = "Sample";
    private SpeechRecognizer mRecognizer;
    private RecognitionListener mRecognitionListener = new RecognitionListener() {
        @Override
        public void onError(int error) {
            if ((error == SpeechRecognizer.ERROR_NO_MATCH) ||
                    (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                startSpeechRecognition();
                return;
            }
            State.setText("認識エラー");
            Log.d(TAG, "Recognition Error: " + error);

        }
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> values = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//            String val = values.get(0);
            String val = String.join(",", values);

            State.setText("認識終了");
            Result.setText(val);

            Log.d(TAG, "認識結果: " + val);
            if (val.contains("記録") || val.contains("きろく")){
                Flag.setBackgroundColor(Color.rgb(120, 0 ,0));
            }

            if (val.contains("測定")){
                FlagResult1.setBackgroundColor(Color.rgb(120, 0,0));
            }else if (val.contains("吸引")){
                FlagResult2.setBackgroundColor(Color.rgb(120, 0,0));
            }else if (val.contains("体位") || val.contains("交換")){
                FlagResult3.setBackgroundColor(Color.rgb(120, 0,0));
            }
            handler.postDelayed(Task, 2000);
        }
        @Override public void onBeginningOfSpeech() {}
        @Override public void onBufferReceived(byte[] arg0) {
            Log.d(TAG, "onBufferReceived");
        }
        @Override public void onEndOfSpeech() {}
        @Override public void onEvent(int arg0, Bundle arg1) {
            Log.d(TAG, "onEvent");
        }

        @Override public void onPartialResults(Bundle arg0) {
//            途中経過を返答する
            Log.d(TAG, "onPartialResults");
            ArrayList<String> values = arg0
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//            String val = values.get(0);
            String val = String.join(",", values);

            TakeResult.setText(val);
            Log.d(TAG, "途中認識結果: " + val);
        }
        @Override public void onReadyForSpeech(Bundle arg0) {
            State.setText("話して");
            Log.d(TAG, "onReadyForSpeech");
        }
        @Override public void onRmsChanged(float arg0) {}
    };

    private void startSpeechRecognition() {

        Flag.setBackgroundColor(Color.rgb(255,255,255));
        FlagResult1.setBackgroundColor(Color.rgb(255,255,255));
        FlagResult2.setBackgroundColor(Color.rgb(255,255,255));
        FlagResult3.setBackgroundColor(Color.rgb(255,255,255));

        // Need to destroy a recognizer to consecutive recognition?
        if (mRecognizer != null) {
            mRecognizer.destroy();
        }

        // Create a recognizer.
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(mRecognitionListener);

        // Start recognition.
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        mRecognizer.startListening(intent);
    }
}