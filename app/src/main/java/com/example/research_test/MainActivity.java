package com.example.research_test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.speech.tts.TextToSpeech;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    TextView State;
    TextView Result;
    TextView TakeResult;

    TextView Flag;
    TextView FlagResult1;
    TextView FlagResult2;
    TextView FlagResult3;

    TextToSpeech tts;
    String contents = "読み上げたい内容";

    private int count = 0;
    private final Context context = this;
    private final Handler handler = new Handler();
    private final Runnable Task = new Runnable() {
        @Override
        public void run() {
            startSpeechRecognition();
        }
    };

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothHeadset mBluetoothHeadset;

    AudioManager audioManager;


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

//        認識スタートを遅らせる
//        handler.postDelayed(Task, 1000);

        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

//        プロファイルの変更
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        audioManager.setMode(AudioManager.MODE_IN_CALL);

        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);

        mBluetoothAdapter.getProfileProxy(this,mProfileListener, BluetoothProfile.HEADSET);


        tts = new TextToSpeech(this, this);
        startSpeechRecognition();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //SecondActivityから戻ってきた場合
            case (1):
                if (resultCode == RESULT_OK) {
                    //OKボタンを押して戻ってきたときの処理
                    Log.d("OK", "OK");
                } else if (resultCode == RESULT_CANCELED) {
                    //キャンセルボタンを押して戻ってきたときの処理
                } else {
                    //その他
                }
                break;
            default:
                break;
        }
    }

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
                //現在接続中のデバイス取得
                List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
                if(devices.size() > 0){
                    BluetoothDevice device = devices.get(0);
                    mBluetoothHeadset.startVoiceRecognition(device);
                }

            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        registerReceiver(mReceiver, filter);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("bluetooth", action);
            BluetoothDevice device = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);

            int state = intent.getExtras().getInt(BluetoothProfile.EXTRA_STATE);
            int prevState = intent.getExtras().getInt(BluetoothProfile.EXTRA_PREVIOUS_STATE);

            if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)){
                    String stateStr = state == BluetoothHeadset.STATE_CONNECTED ? "CONNECTED"
                            : state == BluetoothHeadset.STATE_CONNECTING ? "CONNECTING"
                            : state == BluetoothHeadset.STATE_DISCONNECTED ? "DISCONNECTED"
                            : state == BluetoothHeadset.STATE_DISCONNECTING ? "DISCONNECTING"
                            : "Unknown";
                    String prevStateStr = prevState == BluetoothHeadset.STATE_CONNECTED ? "CONNECTED"
                            : prevState == BluetoothHeadset.STATE_CONNECTING ? "CONNECTING"
                            : prevState == BluetoothHeadset.STATE_DISCONNECTED ? "DISCONNECTED"
                            : prevState == BluetoothHeadset.STATE_DISCONNECTING ? "DISCONNECTING"
                            : "Unknown";

                    Log.e(TAG, "BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: EXTRA_DEVICE=" + device.getName() + " EXTRA_STATE=" + stateStr + " EXTRA_PREVIOUS_STATE=" + prevStateStr);

                    if (state == BluetoothHeadset.STATE_CONNECTED){
                        mBluetoothHeadset.startVoiceRecognition(device);
                    }else if (prevState == BluetoothHeadset.STATE_CONNECTED){
                        mBluetoothHeadset.stopVoiceRecognition(device);
                    }

                }
                Log.e("bluetooth", action);

            }else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                String stateStr = state == BluetoothHeadset.STATE_AUDIO_CONNECTED ? "AUDIO_CONNECTED"
                        : state == BluetoothHeadset.STATE_AUDIO_CONNECTING ? "AUDIO_CONNECTING"
                        : state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED ? "AUDIO_DISCONNECTED"
                        : "Unknown";
                String prevStateStr = prevState == BluetoothHeadset.STATE_AUDIO_CONNECTED ? "AUDIO_CONNECTED"
                        : prevState == BluetoothHeadset.STATE_AUDIO_CONNECTING ? "AUDIO_CONNECTING"
                        : prevState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED ? "AUDIO_DISCONNECTED"
                        : "Unknown";
                Log.e(TAG, "BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED: EXTRA_DEVICE=" + device.getName() + " EXTRA_STATE=" + stateStr + " EXTRA_PREVIOUS_STATE=" + prevStateStr);
            }

        }
    };

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

//            読み上げ
            contents = values.get(0);
            speechText();
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

    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            //言語選択
            Locale locale = Locale.JAPAN;
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);
            } else {
                Log.d("Error", "Locale");
            }
        } else {
            Log.d("Error", "Init");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != tts) {
            //ttsのリソース解放する
            tts.shutdown();
        }
        audioManager.stopBluetoothSco();
    }
    private void speechText() {
        if (0 < contents.length()) {
            if (tts.isSpeaking()) {
                // 読み上げ中なら停止
                tts.stop();
            }
            //読み上げられているテキストを確認
            System.out.println(contents);
            //読み上げ開始
//            tts.speak(contents, TextToSpeech.QUEUE_FLUSH, null);
            setTtsListener();
            tts.speak(contents, TextToSpeech.QUEUE_FLUSH, null, "messageID");
        }
    }
    // 読み上げの始まりと終わりを取得
    private void setTtsListener(){
        if (Build.VERSION.SDK_INT >= 21){
            int listenerResult =
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            Log.d(TAG,"progress on Done " + utteranceId);
                            handler.post(Task);
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.d(TAG,"progress on Error " + utteranceId);
                        }

                        @Override
                        public void onStart(String utteranceId) {
                            Log.d(TAG,"progress on Start " + utteranceId);
                        }
                    });

            if (listenerResult != TextToSpeech.SUCCESS) {
                Log.e(TAG, "failed to add utterance progress listener");
            }
        }
        else {
            Log.e(TAG, "Build VERSION is less than API 15");
        }
    }
}