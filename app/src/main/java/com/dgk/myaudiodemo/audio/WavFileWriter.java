/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    WavFileWriter.java
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.dgk.myaudiodemo.audio;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
