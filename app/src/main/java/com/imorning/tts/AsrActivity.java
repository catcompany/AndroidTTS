package com.imorning.tts;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.google.gson.Gson;
import com.imorning.tts.utils.AsrFinishJsonData;
import com.imorning.tts.utils.AsrPartialJsonData;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class AsrActivity extends AppCompatActivity implements EventListener {

    private static final String TAG = "AsrActivity";

    private Button btnStartRecord;
    private Button btnStopRecord;
    private TextView tvParseResult;
    private EventManager asr;

    private String final_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asr);
        initView();
        asr = EventManagerFactory.create(this, "asr");
        asr.registerListener(this); //  EventListener 中 onEvent方法

        btnStartRecord.setOnClickListener(v -> start());
        btnStopRecord.setOnClickListener(v -> stop());
    }

    private void initView() {
        tvParseResult = (TextView) findViewById(R.id.tvParseResult);
        btnStartRecord = (Button) findViewById(R.id.btnStartRecord);
        btnStopRecord = (Button) findViewById(R.id.btnStopRecord);
        btnStopRecord.setVisibility(View.GONE);
    }


    /*
     * EventListener  回调方法
     * name:回调事件
     * params: JSON数据，其格式如下：
     *
     * */
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String result = "";

        if (length > 0 && data.length > 0) {
            result += ", 语义解析结果：" + new String(data, offset, length);
        }

        switch (name) {
            case SpeechConstant.CALLBACK_EVENT_ASR_READY:
                // 引擎准备就绪，可以开始说话
                result += "引擎准备就绪，可以开始说话";

                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_BEGIN:
                // 检测到用户的已经开始说话
                result += "检测到用户的已经开始说话";
                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_END:
                // 检测到用户的已经停止说话
                result += "检测到用户的已经停止说话";
                if (params != null && !params.isEmpty()) {
                    result += "params :" + params + "\n";
                }
                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL:
                // 临时识别结果, 长语音模式需要从此消息中取出结果
                result += "识别临时识别结果";
                if (params != null && !params.isEmpty()) {
                    result += "params :" + params + "\n";
                }
                parseAsrPartialJsonData(params);
                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_FINISH:
                // 识别结束， 最终识别结果或可能的错误
                result += "识别结束";
                btnStartRecord.setEnabled(true);
                asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
                if (params != null && !params.isEmpty()) {
                    result += "params :" + params + "\n";
                }
                Log.d(TAG, "Result Params:" + params);
                parseAsrFinishJsonData(params);
                break;
        }
        Log.d(TAG, "onEvent: " + result);
        tvParseResult.setText(final_result);
    }


    private void start() {
        btnStartRecord.setEnabled(false);
        Map<String, Object> params = new LinkedHashMap<>();
        String event;
        event = SpeechConstant.ASR_START;
        params.put(SpeechConstant.PID, 1537); // 默认1536
        params.put(SpeechConstant.DECODER, 0); // 纯在线(默认)
        params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN); // 语音活动检测
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 2000); // 不开启长语音。开启VAD尾点检测，即静音判断的毫秒数。建议设置800ms-3000ms
        params.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);// 是否需要语音音频数据回调
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);// 是否需要语音音量数据回调

        String json; //可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
    }

    private void stop() {
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
    }


    private void parseAsrPartialJsonData(String data) {
        Log.d(TAG, "parseAsrPartialJsonData data:" + data);
        Gson gson = new Gson();
        AsrPartialJsonData jsonData = gson.fromJson(data, AsrPartialJsonData.class);
        String resultType = jsonData.getResult_type();
        Log.d(TAG, "resultType:" + resultType);
        if (resultType != null && resultType.equals("final_result")) {
            final_result = jsonData.getBest_result();
            // tvParseResult.setText("解析结果：" + final_result);
        }
    }

    private void parseAsrFinishJsonData(String data) {
        Log.d(TAG, "parseAsrFinishJsonData data:" + data);
        Gson gson = new Gson();
        AsrFinishJsonData jsonData = gson.fromJson(data, AsrFinishJsonData.class);
        String desc = jsonData.getDesc();
        if (desc != null && desc.equals("Speech Recognize success.")) {
            tvParseResult.setText("解析结果:" + final_result);
        } else {
            String errorCode = "\n错误码:" + jsonData.getError();
            String errorSubCode = "\n错误子码:" + jsonData.getSub_error();
            String errorResult = errorCode + errorSubCode;
            tvParseResult.setText("解析错误,原因是:" + desc + "\n" + errorResult);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        asr.unregisterListener(this);
    }
}
