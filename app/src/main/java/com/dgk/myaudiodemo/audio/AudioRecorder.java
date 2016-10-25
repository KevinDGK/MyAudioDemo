package com.dgk.myaudiodemo.audio;

import android.media.AudioRecord;

import com.dgk.myaudiodemo.util.CommUtil;
import com.dgk.myaudiodemo.util.LogUtil;

/**
 * Created by Kevin on 2016/10/24.
 * 录音机
 */
public class AudioRecorder {

    private static final String tag = "【AudioRecorder】";

    private int DEFAULT_SAMPLERATEINHZ = AudioConfig.sampleRateInHz;       // 采样频率
    private int DEFAULT_CHANNELCONFIG_IN = AudioConfig.channelConfigIn;    // 声道配置
    private int DEFAULT_AUDIOFORMAT = AudioConfig.audioFormat;             // 音频格式
    private int DEFAULT_AUDIOSOURCE = AudioConfig.audioSource;             // 音频来源

    private RecorderThread recorderThread;  // 录音线程
    private AudioRecord recorder;           // 录音对象
    private boolean isRunning;              // 录音线程是否运行
    private boolean isWorking;              // 录音线程是否工作(录音)

    private onRecorderListener recorderListener;
    private int recordBufferSize;

    public AudioRecorder(onRecorderListener recorderListener) {
        this.recorderListener = recorderListener;
        init();
    }

    /** 初始化 */
    private void init() {

        LogUtil.i(tag, "开始创建录音对象...");

        //1. 获取最小的缓冲区大小
        recordBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLERATEINHZ,
                DEFAULT_CHANNELCONFIG_IN, DEFAULT_AUDIOFORMAT);
        switch (recordBufferSize) {
            case AudioRecord.ERROR_BAD_VALUE:
                LogUtil.i(tag, "无效的音频参数");
                break;
            case AudioRecord.ERROR:
                LogUtil.i(tag, "不能够查询音频输入的性能");
                break;
            default:
                LogUtil.i(tag, "AudioRecord的音频缓冲区的最小尺寸(与本机硬件有关)：" + recordBufferSize);
                break;
        }

        //2. 创建AudioRecord实例
        recorder = new AudioRecord(DEFAULT_AUDIOSOURCE, DEFAULT_SAMPLERATEINHZ,
                DEFAULT_CHANNELCONFIG_IN, DEFAULT_AUDIOFORMAT, recordBufferSize * 4);
        switch (recorder.getState()) {
            case AudioRecord.STATE_INITIALIZED:
                LogUtil.i(tag, "AudioTrack实例初始化成功!");
                break;
            case AudioRecord.STATE_UNINITIALIZED:
                LogUtil.i(tag, "AudioTrack实例初始化失败!");
                break;
        }

        //3. 创建录音线程
        isRunning = true;
        isWorking = false;
        recorderThread = new RecorderThread();
    }

    /** 开始录音 */
    public void start() {

        if (isWorking) {
            CommUtil.Toast("正在录音中!");
            return;
        }

        try {
            //4. 开始录音
            recorderThread.start();
            recorder.startRecording();
            isWorking = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /** 停止录音 */
    public void stop() {

        if (!isWorking) {
            CommUtil.Toast("已经停止录音!");
        }

        //6. 停止录音
        try {

            recorder.stop();
            recorder.release();
            recorder = null;

            isWorking = false;
            recorderThread.interrupt();
            recorderThread.join(1000);  // 先停止录音线程，然后延时1s再停止播放器，防止程序崩溃

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 录音线程 */
    private class RecorderThread extends Thread {

        private byte[] recordData = new byte[recordBufferSize];   // 读取音频数据存放的数组

        @Override
        public void run() {
            super.run();

            while (isRunning) {

                if (isWorking) {

                    //5. 读取语音信息
                    int readNumber = recorder.read(recordData, 0, recordData.length);
                    switch (readNumber) {
                        case AudioRecord.ERROR_INVALID_OPERATION:
                            LogUtil.i(tag, "读取语音信息...发现实例初始化失败！");
                            break;
                        case AudioRecord.ERROR_BAD_VALUE:
                            LogUtil.i(tag, "读取语音信息...发现参数无效！");
                            break;
                        default:
                            LogUtil.i(tag, "读取到的语音数据的长度：" + readNumber + " Shorts");
                            recorderListener.handleRecordData(recordData, 0, readNumber);
                            break;
                    }
                }
            }
        }
    }

    /** 获取录音数据接口 */
    public interface onRecorderListener {
        void handleRecordData(byte[] recordData, int offset, int size);
    }
}