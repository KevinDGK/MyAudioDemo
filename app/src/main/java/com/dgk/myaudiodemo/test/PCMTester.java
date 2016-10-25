package com.dgk.myaudiodemo.test;

import com.dgk.myaudiodemo.audio.AudioPlayer;
import com.dgk.myaudiodemo.audio.AudioRecorder;

/**
 * Created by Kevin on 2016/10/24.
 */
public class PCMTester implements BaseTester, AudioRecorder.onRecorderListener {

    private static final String tag = "【PCMTester】";

    private AudioRecorder recorder;
    private AudioPlayer player;

    @Override
    public void start() {

        player = new AudioPlayer();         // 创建播放器对象
        recorder = new AudioRecorder(this); // 创建录音对象
        recorder.start();                   // 开始录音
    }

    @Override
    public void stop() {

        recorder.stop();    // 停止录音
        player.stop();      // 停止播放
    }

    @Override
    public void handleRecordData(byte[] recordData, int offset, int size) {

        // 将录音捕捉的音频数据写入到播放器中播放
        if (player != null) {
            player.play(recordData, offset, size);
        }
    }
}
