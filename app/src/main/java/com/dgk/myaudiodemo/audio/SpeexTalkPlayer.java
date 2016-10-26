package com.dgk.myaudiodemo.audio;

import android.media.AudioFormat;
import android.media.AudioTrack;

import com.dgk.myaudiodemo.util.CommUtil;
import com.dgk.myaudiodemo.util.LogUtil;
import com.dgk.myaudiodemo.util.Speex;

/**
 * Created by Kevin on 2016/10/24.
 * 音频播放器
 */
public class SpeexTalkPlayer {

    private static final String tag = "【SpeexTalkPlayer】";

    private int DEFAULT_SAMPLERATEINHZ = 8000;        // 采样频率
    private int DEFAULT_AUDIOFORMAT = AudioConfig.audioFormat;              // 数据格式
    private int DEFAULT_STREAMTYPE = AudioConfig.streamType;                // 音频类型
    private int DEFAULT_CHANNELCONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;   // 声道配置
    private int DEFAULT_MODE = AudioConfig.mode;                            // 输出模式
    /**
     * 音频帧数据长度
     *  通常一个音频帧为20ms内的音频数据，
     *  由于 码率 = 8K * 16bit * 1 = 108Kbps = 16KBps，
     *  所以 音频帧数据大小为 int size = 20ms * 16KBps = 320 Byte = 160 Short，
     *  即每个音频帧的数据大小为 320个字节，或者为160个Short。
     */
    private int audioShortArrayLength = 160;

    private AudioTrack player;      // 播放器实例
    private boolean isWorking;      // 是否正在工作
    private int playerBufferSize;   // 缓冲区大小

    private Speex speex;                    // Speex音频编解码器
    private short[] decodedShorts = new short[audioShortArrayLength];   // 解码后的音频数据

    public SpeexTalkPlayer() {
        init();
    }

    /** 初始化 */
    private void init() {

        //1. 获取最小缓冲区大小
        playerBufferSize = AudioTrack.getMinBufferSize(DEFAULT_SAMPLERATEINHZ,
                DEFAULT_CHANNELCONFIG_OUT, DEFAULT_AUDIOFORMAT);
        switch (playerBufferSize) {
            case AudioTrack.ERROR_BAD_VALUE:
                LogUtil.i(tag, "无效的音频参数");
                break;
            case AudioTrack.ERROR:
                LogUtil.i(tag, "不能够查询音频输出的性能");
                break;
            default:
                LogUtil.i(tag, "AudioTrack的音频缓冲区的最小尺寸(与本机硬件有关)：" + playerBufferSize);
                break;
        }

        //2. 创建AudioTrack实例
        player = new AudioTrack(DEFAULT_STREAMTYPE, DEFAULT_SAMPLERATEINHZ, DEFAULT_CHANNELCONFIG_OUT,
                DEFAULT_AUDIOFORMAT, playerBufferSize * 4, DEFAULT_MODE);
        switch (player.getState()) {
            case AudioTrack.STATE_INITIALIZED:
                LogUtil.i(tag, "AudioTrack实例初始化成功!");
                break;
            case AudioTrack.STATE_UNINITIALIZED:
                LogUtil.i(tag, "AudioTrack实例初始化失败!");
                break;
            case AudioTrack.STATE_NO_STATIC_DATA:
                LogUtil.i(tag, "AudioTrack实例初始化成功，目前没有静态数据输入!");
                break;
        }
        LogUtil.i(tag, "当前AudioTrack实例的播放状态：" + player.getPlayState());

        //3. 创建Speex编解码实例
        speex = new Speex();
        speex.open(4);

        //4. 设置开始工作
        isWorking = true;
    }

    /** 播放语音数据 */
    public void play(byte[] audioData) {

        if (!isWorking) {
            CommUtil.Toast("AudioTrack is not working!");
            return;
        }

        //5. 对音频数据解码
        int decode = speex.decode(audioData, decodedShorts, audioData.length);

        try {
            //6. 写入数据
            int write = player.write(decodedShorts, 0, decode);

            if (write < 0) {
                LogUtil.i(tag, "write失败");
                switch (write) {
                    case AudioTrack.ERROR_INVALID_OPERATION:    // -3
                        LogUtil.i(tag, "AudioTrack实例初始化失败!");
                        break;
                    case AudioTrack.ERROR_BAD_VALUE:            // -2
                        LogUtil.i(tag, "无效的音频参数");
                        break;
                    case AudioTrack.ERROR:                      // -1
                        LogUtil.i(tag, "通用操作失败");
                        break;
                }
            } else {
                LogUtil.i(tag, "成功写入数据：" + decode + " Shorts");
            }

            //7. 播放音频数据
            player.play();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /** 停止播放语音数据 */
    public void stop() {

        if (!isWorking) {
            CommUtil.Toast("已经停止播放!");
            return;
        }

        //6. 停止播放
        try {
            player.stop();
            player.release();
            player = null;

            isWorking = false;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMinBufferSize() {
        return playerBufferSize;
    }
}