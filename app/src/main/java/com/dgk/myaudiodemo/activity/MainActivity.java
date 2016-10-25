package com.dgk.myaudiodemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dgk.myaudiodemo.R;
import com.dgk.myaudiodemo.test.PCMTester;
import com.dgk.myaudiodemo.test.WAVTester;
import com.dgk.myaudiodemo.util.CommUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private PCMTester pcmTester;
    private WAVTester wavTester;
    private boolean isPCMTesting;
    private boolean isWAVTesting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_pcm_recorder_start, R.id.btn_pcm_recorder_end,
            R.id.btn_wav_recorder_start, R.id.btn_wav_recorder_end, R.id.btn_wav_player})
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
        }
    }
}
