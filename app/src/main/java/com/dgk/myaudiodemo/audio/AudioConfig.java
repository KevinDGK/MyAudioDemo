package com.dgk.myaudiodemo.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;

/**
 * Created by Kevin on 2016/10/24.
 * 音频配置文件
 */
public class AudioConfig {

    public static final int sampleRateInHz = 44100;                            // 采样频率
    public static final int channelConfigIn = AudioFormat.CHANNEL_IN_STEREO;   // 双声道输入(立体声)
    public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;      // 16bit
    public static final int audioSource = MediaRecorder.AudioSource.MIC;       // mic

    public static final int streamType = AudioManager.STREAM_MUSIC;            // 音频类型
    public static final int channelConfigOut = AudioFormat.CHANNEL_OUT_STEREO; // 双声道输出
    public static final int mode = AudioTrack.MODE_STREAM;                     // 输出模式

    public static final String DEFAULT_WAV_PATH = Environment.getExternalStorageDirectory() + "/test.wav";

    public static final int SPEEX_DATA_SIZE = 20;
}
