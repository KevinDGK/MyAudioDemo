> 版权声明：本文为原创文章，未经允许不得转载 
>
> 博客地址：http://blog.csdn.net/kevindgk  
>
> GitHub地址：https://github.com/KevinDGK/MyAudioDemo



# 一、录制和播放PCM音频流

## 1.录制

- 抽取音频配置文件

```java
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

    public static final String DEFAULT_WAV_PATH = Environment.getExternalStorageDirectory() + "/test.wav";	// WAV音频文件保存路径
}
```

- 配置AudioRecord

```java
    private int DEFAULT_SAMPLERATEINHZ = AudioConfig.sampleRateInHz;       // 采样频率
    private int DEFAULT_CHANNELCONFIG_IN = AudioConfig.channelConfigIn;    // 声道配置
    private int DEFAULT_AUDIOFORMAT = AudioConfig.audioFormat;             // 音频格式
    private int DEFAULT_AUDIOSOURCE = AudioConfig.audioSource;             // 音频来源
```

注意：

① 采样频率为44.1KHz，位宽为16bit，录制出来的PCM音频一般称为无损音频，也是传统的CD格式；

② 声道配置为立体声(双声道)，音频来源为麦克风；

③ 输出模式为音频流模式，表示不是直接播放音频文件，而是播放音频数据流；

如果对于各个参数 不是很理解或者音频相关的基础较弱，可以先看前一篇的音频基础。

- 音频录制的流程

```
1. 获取最小的缓冲区大小
2. 创建AudioRecord实例
3. 创建录音线程
4. 开始录音
5. 读取语音信息
6. 停止录音
```

- 代码

```java
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
```

- 注意事项

① 我们可以通过AudioRecord.getMinBufferSize()方法来获取最小的缓冲区大小recordBufferSize，和采样率、声道配置、音频格式和手机硬件有关，我们创建AudioRecord实例的时候需要传入缓冲区大小来创建一个音频缓冲区，用来存放MIC录制的音频数据，这个大小一般为recordBufferSize的倍数，比如2~4倍。如果这个缓冲区过小，当MIC写入速度大于读取速度的时候就会抛出异常。

② 通过AudioRecord获取的是原生的PCM音频数据，我们可以直接对原始数据操作，比如将其编码压缩，保存成其他的音频格式，也可以直接网络传输该数据流。但是，需要注意的就是，在这种情况下，码率 = 44.1K * 16 *2 =1411.2Kbps = 176.4KBps，也就是说，每秒占用的带宽就会达到176.4KB，相当的耗费流量。如果要用于局域网语音通信或者互联网语音电话，我们还需要进行修改配置和进行编解码，详见下一篇局域网语音通信和音频压缩。

③ 设置录音数据接口，用来实时从缓冲区读取音频数据。

## 2.播放

- 配置AudioTrack

```java
    private int DEFAULT_SAMPLERATEINHZ = AudioConfig.sampleRateInHz;        // 采样频率
    private int DEFAULT_AUDIOFORMAT = AudioConfig.audioFormat;              // 数据格式
    private int DEFAULT_STREAMTYPE = AudioConfig.streamType;                // 音频类型
    private int DEFAULT_CHANNELCONFIG_OUT = AudioConfig.channelConfigOut;   // 声道配置
    private int DEFAULT_MODE = AudioConfig.mode;                            // 输出模式
```

- 音频播放的流程

```
1. 获取最小缓冲区大小
2. 创建AudioTrack实例
3. 设置开始工作
4. 写入数据
5. 播放音频数据
6. 停止播放
```

- 代码

```java
    private int DEFAULT_SAMPLERATEINHZ = AudioConfig.sampleRateInHz;        // 采样频率
    private int DEFAULT_AUDIOFORMAT = AudioConfig.audioFormat;              // 数据格式
    private int DEFAULT_STREAMTYPE = AudioConfig.streamType;                // 音频类型
    private int DEFAULT_CHANNELCONFIG_OUT = AudioConfig.channelConfigOut;   // 声道配置
    private int DEFAULT_MODE = AudioConfig.mode;                            // 输出模式

    private AudioTrack player;      // 播放器实例
    private boolean isWorking;      // 是否正在工作
    private int playerBufferSize;   // 缓冲区大小

    public AudioPlayer() {
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

        //3. 设置开始工作
        isWorking = true;
    }

    /** 播放语音数据 */
    public void play(byte[] audioData,int offset, int size) {

        if (!isWorking) {
            CommUtil.Toast("AudioTrack is not working!");
        }

        try {
            //4. 写入数据
            int write = player.write(audioData, offset, size);

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
                LogUtil.i(tag, "成功写入数据：" + size + " Shorts");
            }

            //5. 播放音频数据
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
```

- 注意事项

① 缓冲区大小也是为获取的最小尺寸的2~4倍；

② 录音线程必须在子线程，语音播放的线程可以在子线程也可以在主线程。在这里，为了比较直观，就直接放在了主线程，但是实际开发中，往往需要放到子线程中。

## 3.测试

```java
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
```



------



# 二、录制和播放WAV音频文件

## WAV简介

WAV为微软公司（Microsoft)开发的一种声音文件格式，它符合RIFF(Resource Interchange File Format)文件规范，用于保存Windows平台的音频信息资源，被Windows平台及其应用程序所广泛支持，该格式也支持MSADPCM，CCITT A LAW等多种压缩运算法，支持多种音频数字，取样频率和声道，标准格式化的WAV文件和CD格式一样，也是44.1K的取样频率，16位量化数字，因此在声音文件质量和CD相差无几！

- 文件扩展名：".wav"
- 文件格式：文件头+音频数据流
- 数据流格式：PCM或压缩型

详细的信息和文件头参数可以参考[百度百科](http://baike.baidu.com/link?url=LdbvjusKmY9b4kOklKewaejN2D6i6KuRpNVVB2lfIlhp9UWnFi6G-9nprx38yQeRzpVmSXODBOON23Yv8RV5Z_)，或者自行搜索。

## 1.录制

- 流程

```
1.创建并打开WAV格式的音频文件
2.写入WAV音频文件头信息
3.开始录制，同时将音频数据写入文件
4.停止录制，然后关闭文件
```

其中，WAV格式的数据流可以为PCM音频流，所以我们直接用上面的录制和播放PCM音频的代码，我们主要的工作就是根据格式添加头信息，小编懒得写，直接从大神的代码里Copy了一份，底部有相关博客链接。

- WavFileWriter文件写入类代码如下

```java
public class WavFileWriter {

    private String mFilepath;   // 文件保存路径
    private int mDataSize = 0;  // 数据大小
    private DataOutputStream mDataOutputStream; // 包装数据流

    /**
     * 创建并打开WAV格式的音频文件，并写入头信息
     * @param filepath  文件路径
     * @param sampleRateInHz    采样频率
     * @param bitsPerSample     每个采样点的bit位数
     * @param channels          声道的数量
     * @return
     * @throws IOException
     */
    public boolean openFile(String filepath, int sampleRateInHz, int bitsPerSample, int channels) throws IOException {
        if (mDataOutputStream != null) {
            closeFile();
        }
        mFilepath = filepath;
        mDataSize = 0;
        mDataOutputStream = new DataOutputStream(new FileOutputStream(filepath));
        return writeHeader(sampleRateInHz, bitsPerSample, channels);
    }

    /** 关闭文件 */
    public boolean closeFile() throws IOException {
        boolean ret = true;
        if (mDataOutputStream != null) {
            ret = writeDataSize();
            mDataOutputStream.close();
            mDataOutputStream = null;
        }
        return ret;
    }

    /** 写入音频信息 */
    public boolean writeData(byte[] buffer, int offset, int count) {

        if (mDataOutputStream == null) {
            return false;
        }

        try {
            mDataOutputStream.write(buffer, offset, count);
            mDataSize += count;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /** 写入头信息 */
    private boolean writeHeader(int sampleRateInHz, int bitsPerSample, int channels) {

        if (mDataOutputStream == null) {
            return false;
        }

        WavFileHeader header = new WavFileHeader(sampleRateInHz, bitsPerSample, channels);

        try {
            mDataOutputStream.writeBytes(header.mChunkID);
            mDataOutputStream.write(intToByteArray(header.mChunkSize), 0, 4);
            mDataOutputStream.writeBytes(header.mFormat);
            mDataOutputStream.writeBytes(header.mSubChunk1ID);
            mDataOutputStream.write(intToByteArray(header.mSubChunk1Size), 0, 4);
            mDataOutputStream.write(shortToByteArray(header.mAudioFormat), 0, 2);
            mDataOutputStream.write(shortToByteArray(header.mNumChannel), 0, 2);
            mDataOutputStream.write(intToByteArray(header.mSampleRate), 0, 4);
            mDataOutputStream.write(intToByteArray(header.mByteRate), 0, 4);
            mDataOutputStream.write(shortToByteArray(header.mBlockAlign), 0, 2);
            mDataOutputStream.write(shortToByteArray(header.mBitsPerSample), 0, 2);
            mDataOutputStream.writeBytes(header.mSubChunk2ID);
            mDataOutputStream.write(intToByteArray(header.mSubChunk2Size), 0, 4);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /** 写入数据大小 */
    private boolean writeDataSize() {

        if (mDataOutputStream == null) {
            return false;
        }

        try {
            RandomAccessFile wavFile = new RandomAccessFile(mFilepath, "rw");
            wavFile.seek(WavFileHeader.WAV_CHUNKSIZE_OFFSET);
            wavFile.write(intToByteArray(mDataSize + WavFileHeader.WAV_CHUNKSIZE_EXCLUDE_DATA), 0, 4);
            wavFile.seek(WavFileHeader.WAV_SUB_CHUNKSIZE2_OFFSET);
            wavFile.write(intToByteArray(mDataSize), 0, 4);
            wavFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /** int转byte */
    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    /** short转byte */
    private static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }
}
```

文件头信息详见代码，链接在顶部。



## 2.播放

使用AudioTrack回放音频数据的代码同上，我们主要做的就是读取头信息，具体的详见代码。

## 3.测试

```java
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
```



# 三、运行





# 四、小结

在实际开发中，除视音频应用外，我们使用到音频的播放和录制的场景并不太多，尤其是大部分我们都会直接使用MediaRecorder\MediaPlayer\SoundPool等进行音频的录制和播放，因为大多不需要直接接触到原始的音频数据流，直接录制和播放音频文件，比较方便。但是如果想要实现定制，比如音频的编码和压缩，就需要使用AudioRecord和AudioTrack进行编码，比较更加接近于底层。具体使用哪种方式，视需求而定。

本文介绍的方法都是比较原生的使用，如果想要实现实时传输音频数据流，可以参考下一篇博客，在局域网中实现了语音数据的传输以及音频编码相关的知识。



### 引用

1.[如何存储和解析wav文件 - 卢俊](http://ticktick.blog.51cto.com/823160/1752947)

2.[AudioTrack分析](http://www.cnblogs.com/innost/archive/2011/01/09/1931457.html)



### 联系方式

邮箱：815852777@qq.com

微信：

![](http://i.imgur.com/35cS46S.jpg)  