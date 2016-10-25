package com.dgk.myaudiodemo.test;

import com.dgk.myaudiodemo.audio.AudioConfig;
import com.dgk.myaudiodemo.audio.AudioPlayer;
import com.dgk.myaudiodemo.audio.AudioRecorder;
import com.dgk.myaudiodemo.audio.WavFileReader;
import com.dgk.myaudiodemo.audio.WavFileWriter;

import java.io.IOException;

/**
 * Created by Kevin on 2016/10/24.
 * WAV音频测试类
 */
public class WAVTester implements BaseTester, AudioRecorder.onRecorderListener {

    private static final String tag = "【WAVTester】";

    private AudioRecorder recorder;
    private WavFileWriter wavFileWirter;

    private AudioPlayer player;
    private WavFileReader wavFileReader;
    private onPlayBackListener listener;

    @Override
    public void start() {

        wavFileWirter = new WavFileWriter();    // 创建WAV音频格式文件写入对象

        try {
            wavFileWirter.openFile(AudioConfig.DEFAULT_WAV_PATH,    // 创建并打开WAV音频文件，并写入头信息
                    AudioConfig.sampleRateInHz, 16, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder = new AudioRecorder(this); // 创建录音对象
        recorder.start();                   // 开始录音
    }

    @Override
    public void stop() {

        recorder.stop();                // 停止录音

        try {
            wavFileWirter.closeFile();  // 停止写入数据
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleRecordData(byte[] recordData, int offset, int size) {

        // 将录音捕捉的音频数据写入到文件中
        wavFileWirter.writeData(recordData, offset, size);
    }

    /** 回放wav音频文件 */
    public void playback(onPlayBackListener listener) {

        this.listener = listener;

        wavFileReader = new WavFileReader();
        player = new AudioPlayer();

        try {
            wavFileReader.openFile(AudioConfig.DEFAULT_WAV_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new PlayBackThread().start();
    }

    /** 回放音频文件线程 */
    private class PlayBackThread extends Thread {

        @Override
        public void run() {
            super.run();

            byte[] buffer = new byte[player.getMinBufferSize()];
            while (wavFileReader.readData(buffer, 0, buffer.length) > 0) {
                player.play(buffer, 0, buffer.length);
            }
            player.stop();
            listener.onStop();
            try {
                wavFileReader.closeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** 回放监听器 */
    public interface onPlayBackListener {
        void onStop();
    }
}