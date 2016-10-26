package com.dgk.myaudiodemo.test;

import com.dgk.myaudiodemo.audio.SpeexTalkPlayer;
import com.dgk.myaudiodemo.audio.SpeexTalkRecorder;
import com.dgk.myaudiodemo.audio.TalkPlayer;
import com.dgk.myaudiodemo.audio.TalkRecorder;

/**
 * Created by Kevin on 2016/10/24.
 */
public class SpeexTalkTester implements BaseTester, SpeexTalkRecorder.onRecorderListener {

    private static final String tag = "【TalkUnEncodeTester】";

    private SpeexTalkRecorder recorder;
    private SpeexTalkPlayer player;

    @Override
    public void start() {

        player = new SpeexTalkPlayer();         // 创建播放器对象
        recorder = new SpeexTalkRecorder(this); // 创建录音对象
        recorder.start();                       // 开始录音
    }

    @Override
    public void stop() {

        recorder.stop();    // 停止录音
        player.stop();      // 停止播放
    }

    @Override
    public void handleRecordData(byte[] recordData) {

        // 将录音捕捉的音频数据写入到播放器中播放
        if (player != null) {
            player.play(recordData);
        }
    }
}
