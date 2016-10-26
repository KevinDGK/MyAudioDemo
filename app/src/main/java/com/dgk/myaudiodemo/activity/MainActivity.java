package com.dgk.myaudiodemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dgk.myaudiodemo.R;
import com.dgk.myaudiodemo.test.PCMTester;
import com.dgk.myaudiodemo.test.SpeexTalkTester;
import com.dgk.myaudiodemo.test.UnEncodeTalkTester;
import com.dgk.myaudiodemo.test.WAVTester;
import com.dgk.myaudiodemo.util.CommUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private PCMTester pcmTester;
    private WAVTester wavTester;
    private UnEncodeTalkTester talkUnEncodeTester;
    private SpeexTalkTester speexTalkTester;

    private boolean isPCMTesting;
    private boolean isWAVTesting;
    private boolean isUnEncodeTalkTesting;
    private boolean isSpeexTalkTesting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_pcm_recorder_start, R.id.btn_pcm_recorder_end,
            R.id.btn_wav_recorder_start, R.id.btn_wav_recorder_end, R.id.btn_wav_player,
            R.id.btn_8_recorder_start, R.id.btn_8_recorder_end,
            R.id.btn_speex_recorder_start, R.id.btn_speex_recorder_end})
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_pcm_recorder_start:   // PCM
                if (!isPCMTesting) {
                    CommUtil.Toast("开始录制PCM");
                    pcmTester = new PCMTester();
                    pcmTester.start();
                    isPCMTesting = true;
                }
                break;
            case R.id.btn_pcm_recorder_end:
                if (isPCMTesting) {
                    CommUtil.Toast("停止录制PCM");
                    pcmTester.stop();
                    isPCMTesting = false;
                }
                break;

            case R.id.btn_wav_recorder_start:   // WAV
                CommUtil.Toast("开始录制WMV");
                if (!isWAVTesting) {
                    CommUtil.Toast("开始录制WAV");
                    wavTester = new WAVTester();
                    wavTester.start();
                    isWAVTesting = true;
                }
                break;
            case R.id.btn_wav_recorder_end:
                if (isWAVTesting) {
                    CommUtil.Toast("停止录制WAV");
                    wavTester.stop();
                    isWAVTesting = false;
                }
                CommUtil.Toast("停止录制WMV");
                break;
            case R.id.btn_wav_player:
                if (!isWAVTesting) {
                    CommUtil.Toast("开始播放WAV");
                    if (wavTester == null) {
                        wavTester = new WAVTester();
                    }
                    wavTester.playback(new WAVTester.onPlayBackListener() {
                        @Override
                        public void onStop() {
                            isWAVTesting = false;
                        }
                    });
                    isWAVTesting = true;
                }
                CommUtil.Toast("开始播放WMV");
                break;

            case R.id.btn_8_recorder_start:   // 8KHz/16bit/单声道
                if (!isUnEncodeTalkTesting) {
                    CommUtil.Toast("开始通话");
                    talkUnEncodeTester = new UnEncodeTalkTester();
                    talkUnEncodeTester.start();
                    isUnEncodeTalkTesting = true;
                }
                break;
            case R.id.btn_8_recorder_end:
                if (isUnEncodeTalkTesting) {
                    CommUtil.Toast("停止通话");
                    talkUnEncodeTester.stop();
                    isUnEncodeTalkTesting = false;
                }
                break;

            case R.id.btn_speex_recorder_start:   // Speex
                if (!isSpeexTalkTesting) {
                    CommUtil.Toast("开始通话");
                    speexTalkTester = new SpeexTalkTester();
                    speexTalkTester.start();
                    isSpeexTalkTesting = true;
                }
                break;
            case R.id.btn_speex_recorder_end:
                if (isSpeexTalkTesting) {
                    CommUtil.Toast("停止通话");
                    speexTalkTester.stop();
                    isSpeexTalkTesting = false;
                }
                break;
        }
    }
}
