package com.example.research_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
//https://qiita.com/KAKKA/items/47927becf3ae35bbb45b

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSpeechRecognition();
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

            Log.d(TAG, "Recognition Error: " + error);

        }
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> values = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//            String val = values.get(0);
            String val = String.join(",", values);
            Log.d(TAG, "認識結果: " + val);

            startSpeechRecognition();
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
            String val = values.get(0);
            Log.d(TAG, "途中認識結果: " + val);
        }
        @Override public void onReadyForSpeech(Bundle arg0) {
            Log.d(TAG, "onReadyForSpeech");
        }
        @Override public void onRmsChanged(float arg0) {}
    };

    private void startSpeechRecognition() {
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