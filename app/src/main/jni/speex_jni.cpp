#include "jni.h"

#include "string.h"
#include "unistd.h"

#include "include/speex/speex.h"

#include <android/log.h>

/*
    在C语言中标准输出的方法是printf，但是打印出来的内容在logcat看不到，需要使用
    __android_log_print()方法打印log，才能在logcat看到，由于该方法名比较长，我们在
    这里需要定义宏，使得在C语言中能够向Android一样打印log。
    注意：该方法还需要在gradle中声明ldLibs "log"，详见build.gradle
*/
#define  LOG_TAG    "【** C **】"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static int codec_open = 0;		// 是否已经打开编解码器的标记

static int dec_frame_size;
static int enc_frame_size;

static SpeexBits ebits, dbits;	// 编码SpeexBits变量，解码SpeexBits变量
void *enc_state;				// 编码器状态
void *dec_state;				// 解码器状态

static JavaVM *gJavaVM;			// Java虚拟机

extern "C"
JNIEXPORT jint JNICALL Java_com_dgk_myaudiodemo_util_Speex_open
  (JNIEnv *env, jobject obj, jint compression) {

	LOGI("open()\n");

	int tmp;
	if (codec_open++ != 0)		// 不能重复Open
		return 0;

	speex_bits_init(&ebits);	// 初始化SpeexBits
	speex_bits_init(&dbits);

	/*
	 * speex_nb_mode:窄带模式
	 * speex_wb_mode:宽带模式
	 * speex_uwb_mode:超宽带模式
	 */
	enc_state = speex_encoder_init(&speex_nb_mode);	// 初始化编码器
	dec_state = speex_decoder_init(&speex_nb_mode); // 初始化解码器

	tmp = compression;
	speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);				// 设置压缩质量(0~10)
	speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);// 设置编码器音频帧大小
	speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);// 设置解码器音频帧大小

	return 1;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_dgk_myaudiodemo_util_Speex_encode
    (JNIEnv *env, jobject obj, jshortArray lin, jint offset, jbyteArray encoded, jint size) {

	jshort buffer[enc_frame_size];
	jbyte output_buffer[enc_frame_size];
	int nsamples = (size-1)/enc_frame_size + 1;
	int i, tot_bytes = 0;

	if (!codec_open)
		return 0;

	speex_bits_reset(&ebits);

	for (i = 0; i < nsamples; i++) {
		env->GetShortArrayRegion(lin, offset + i*enc_frame_size, enc_frame_size, buffer);
		speex_encode_int(enc_state, buffer, &ebits);
	}
	//env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
	//speex_encode_int(enc_state, buffer, &ebits);

	tot_bytes = speex_bits_write(&ebits, (char *)output_buffer,
				     enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes,
				output_buffer);

        return (jint)tot_bytes;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_dgk_myaudiodemo_util_Speex_decode
    (JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin, jint size) {

        jbyte buffer[dec_frame_size];
        jshort output_buffer[dec_frame_size];
        jsize encoded_length = size;

	if (!codec_open)
		return 0;

	env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
	speex_bits_read_from(&dbits, (char *)buffer, encoded_length);
	speex_decode_int(dec_state, &dbits, output_buffer);
	env->SetShortArrayRegion(lin, 0, dec_frame_size,
				 output_buffer);

	return (jint)dec_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_dgk_myaudiodemo_util_Speex_getFrameSize
    (JNIEnv *env, jobject obj) {

	if (!codec_open)
		return 0;
	return (jint)enc_frame_size;

}

extern "C"
JNIEXPORT jint JNICALL Java_com_dgk_myaudiodemo_util_Speex_close
    (JNIEnv *env, jobject obj) {

	if (--codec_open != 0)		// 如果没有开启过，则返回0
		return 0;

	speex_bits_destroy(&ebits);	// 回收空间
	speex_bits_destroy(&dbits);
	speex_decoder_destroy(dec_state);
	speex_encoder_destroy(enc_state);

	return 1;
}
