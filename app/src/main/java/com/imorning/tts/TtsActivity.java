package com.imorning.tts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.imorning.tts.utils.Auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TtsActivity extends AppCompatActivity {

    private static final String TAG = "TtsActivity";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private final TtsMode ttsMode = TtsMode.ONLINE;
    protected String appId;
    protected String appKey;
    protected String secretKey;

    // =========== 以下为UI部分 ==================================================
    protected SpeechSynthesizer mSpeechSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appId = Auth.getInstance().getAppId();
        appKey = Auth.getInstance().getAppKey();
        secretKey = Auth.getInstance().getSecretKey();
        setContentView(R.layout.activity_tts);
        initView();
        initTTs();
    }

    /**
     * 注意此处为了说明流程，故意在UI线程中调用。
     * 实际集成中，该方法一定在新线程中调用，并且该线程不能结束。具体可以参考NonBlockSyntherizer的写法
     */
    private void initTTs() {
        // 日志打印在logcat中
        LoggerProxy.printable(true);
        // 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);

        // 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        checkResult(result, "setAppId");
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        checkResult(result, "setApiKey");

        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声  3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "3");
        // 设置合成的音量，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        // 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        checkResult(result, "initTts");

    }

    /*  以下参数每次合成时都可以修改
     *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
     *  设置在线发声音人： 0 普通女声（默认） 1 普通男声  3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
     *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5"); 设置合成的音量，0-9 ，默认 5
     *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5"); 设置合成的语速，0-9 ，默认 5
     *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5"); 设置合成的语调，0-9 ，默认 5
     *
     */
    private void speak(String txt) {
        if (mSpeechSynthesizer == null) {
            Log.i(TAG, "speak: Init failed!");
            return;
        }
        if (TextUtils.isEmpty(txt)) {
            Log.e(TAG, "txt is empty!");
            return;
        }
        int result = mSpeechSynthesizer.speak(txt);
        checkResult(result, "speak");
    }

    private void stop() {
        int result = mSpeechSynthesizer.stop();
        checkResult(result, "stop");
    }

    // 初始化界面
    private void initView() {
        Button mSpeak = this.findViewById(R.id.speak);
        Button mStop = this.findViewById(R.id.stop);
        EditText editText = this.findViewById(R.id.et_text);
        View.OnClickListener listener = v -> {
            int id = v.getId();
            switch (id) {
                case R.id.speak:
                    speak(editText.getEditableText().toString());
                    break;
                case R.id.stop:
                    stop();
                    break;
                default:
                    break;
            }
        };
        mSpeak.setOnClickListener(listener);
        mStop.setOnClickListener(listener);

    }

    @Override
    protected void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
        }
        super.onDestroy();
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.i(TAG, "error code :" + result + " method:" + method);
        }
    }


}