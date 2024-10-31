#include <stdint.h>
#include <stdio.h>
#include <windows.h>
#include <string.h>
#include "ttsapi.h"
#include "../../../build/generated/sources/headers/java/main/latibot_audio_DecTalkWrapper.h"

#define TTS_START_SUCCESS 0xF000
#define TTS_START_UNKNOWN_ERROR 0xF0FF

#define TTS_LANG_START_FUNC_NULL 0xF0F1
#define TTS_LANG_SELECT_FUNC_NULL 0xF0F2
#define TTS_STARTUP_FUNC_NULL 0xF0F3
#define TTS_SPEAK_FUNC_NULL 0xF0F4
#define TTS_ENUM_LANGS_FUNC_NULL 0xF0F5
#define TTS_OPEN_WAVEOUT_FUNC_NULL 0xF0F6
#define TTS_SYNC_FUNC_NULL 0xF0F7
#define TTS_CLOSE_WAVEOUT_FUNC_NULL 0xF0F8
#define TTS_SHUTDOWN_FUNC_NULL 0xF0F9

#define TTS_LANG_START_NOT_SUPPORTED 0xF011
#define TTS_LANG_START_NOT_AVAILBLE 0xF012
#define TTS_LANG_START_UNKNOWN_ERROR 0xF013

#define TTS_STARTUP_FAILED 0xF021

#define TTS_SPEAK_STRING_ERR 0xF031
#define TTS_SPEAK_FILE_NAME_ERR 0xF032

LPTTS_HANDLE_T globalTTSRef;
MMRESULT status;
//LANG_ENUM *dt_langs;

JNIEXPORT jint JNICALL Java_latibot_audio_DecTalkWrapper_TextToSpeechStartup(JNIEnv * env, jobject thisObj) {
	//Load library
	HMODULE dectalkDLL = LoadLibrary("dectalk.dll");
	
	//define functions from library
	DWORD (*TextToSpeechEnumLangs)(LPLANG_ENUM*);
	unsigned int (*TextToSpeechStartLang)(char*);
	BOOL (*TextToSpeechSelectLang)(LPTTS_HANDLE_T, unsigned int);
	MMRESULT (*TextToSpeechStartup)(HWND, LPTTS_HANDLE_T*, UINT, DWORD);
	
	//assign function from library
	TextToSpeechEnumLangs = (DWORD (*)(LPLANG_ENUM*))GetProcAddress(dectalkDLL, "TextToSpeechEnumLangs");
	TextToSpeechStartLang = (unsigned int (*)(char*))GetProcAddress(dectalkDLL, "TextToSpeechStartLang");
	TextToSpeechSelectLang = (BOOL (*)(LPTTS_HANDLE_T, unsigned int))GetProcAddress(dectalkDLL, "TextToSpeechSelectLang");
	TextToSpeechStartup = (MMRESULT (*)(HWND, LPTTS_HANDLE_T*, UINT, DWORD))GetProcAddress(dectalkDLL, "TextToSpeechStartup");

	//enum langs avalible
	// if (TextToSpeechEnumLangs != NULL) {
	// 	TextToSpeechEnumLangs(&dt_langs);
	// } else {
	// 	return TTS_ENUM_LANGS_FUNC_NULL;
	// }
	
	//set and select langs
	// if (TextToSpeechStartLang != NULL) {
	// 	unsigned int TTS_lang = TextToSpeechStartLang("us");
	//
	// 	if (TTS_lang & TTS_LANG_ERROR) { //error setting tts lang
	// 		if (TTS_lang == TTS_NOT_SUPPORTED) {
	// 			return TTS_LANG_START_NOT_SUPPORTED;
	// 		} else if (TTS_lang == TTS_NOT_AVAILABLE) {
	// 			return TTS_LANG_START_NOT_AVAILBLE;
	// 		} else {
	// 			return TTS_LANG_START_UNKNOWN_ERROR;
	// 		}
	// 	} else { //no error
	// 		if (TextToSpeechSelectLang != NULL) {
	// 			TextToSpeechSelectLang(NULL, TTS_lang);
	// 		} else {
	// 			return TTS_LANG_SELECT_FUNC_NULL;
	// 		}
	// 	}
	// } else { //error case start lang func is null
	// 	return TTS_LANG_START_FUNC_NULL;
	// }

	//startup tts
	if (TextToSpeechStartup != NULL) {
		status = TextToSpeechStartup(NULL, &globalTTSRef, 0xffffffff, DO_NOT_USE_AUDIO_DEVICE);
		return status;
	}
	return TTS_START_UNKNOWN_ERROR;
}

JNIEXPORT jint JNICALL Java_latibot_audio_DecTalkWrapper_TextToSeechSpeak(JNIEnv * env, jobject thisObj, jstring text, jstring fileName) {
	//Load library
	HMODULE dectalkDLL = LoadLibrary("dectalk.dll");
	
	//define and assign library funcs
	MMRESULT (*TextToSpeechSpeak)(LPTTS_HANDLE_T, LPSTR, DWORD);
	MMRESULT (*TextToSpeechOpenWaveOutFile)(LPTTS_HANDLE_T, char*, DWORD);
	MMRESULT (*TextToSpeechSync)(LPTTS_HANDLE_T);
	MMRESULT (*TextToSpeechCloseWaveOutFile)(LPTTS_HANDLE_T);
	TextToSpeechSpeak = (MMRESULT (*)(LPTTS_HANDLE_T, LPSTR, DWORD))GetProcAddress(dectalkDLL, "TextToSpeechSpeak");
	TextToSpeechOpenWaveOutFile = (MMRESULT (*)(LPTTS_HANDLE_T, char*, DWORD))GetProcAddress(dectalkDLL, "TextToSpeechOpenWaveOutFile");
	TextToSpeechSync = (MMRESULT (*)(LPTTS_HANDLE_T))GetProcAddress(dectalkDLL, "TextToSpeechSync");
	TextToSpeechCloseWaveOutFile = (MMRESULT (*)(LPTTS_HANDLE_T))GetProcAddress(dectalkDLL, "TextToSpeechCloseWaveOutFile");

	if (TextToSpeechSpeak == NULL) return TTS_SPEAK_FUNC_NULL;
	if (TextToSpeechOpenWaveOutFile == NULL) return TTS_OPEN_WAVEOUT_FUNC_NULL;
	if (TextToSpeechSync == NULL) return TTS_SYNC_FUNC_NULL;
	if (TextToSpeechCloseWaveOutFile == NULL) return TTS_CLOSE_WAVEOUT_FUNC_NULL;

	//get cstring of text to speak
	char* str = (*env)->GetStringUTFChars(env, text, NULL);
	if (str == NULL) return TTS_SPEAK_STRING_ERR;
	//get cstring of wav file name
	char* file = (*env)->GetStringUTFChars(env, fileName, NULL);
	if (file == NULL) return TTS_SPEAK_FILE_NAME_ERR;

	status = TextToSpeechOpenWaveOutFile(globalTTSRef, file, WAVE_FORMAT_1M16);
	status = TextToSpeechSpeak(globalTTSRef, str, TTS_FORCE);
	// status = TextToSpeechSpeak(globalTTSRef, "       ", TTS_FORCE);
	status = TextToSpeechSync(globalTTSRef);
	status = TextToSpeechCloseWaveOutFile(globalTTSRef); 

	//free resources
	(*env)->ReleaseStringUTFChars(env,text,str);
	(*env)->ReleaseStringUTFChars(env,fileName,file);
	
	return status;
}

JNIEXPORT jint JNICALL Java_latibot_audio_DecTalkWrapper_TextToSpeechShutdown(JNIEnv * env, jobject thisObj) {
	//Load library
	HMODULE dectalkDLL = LoadLibrary("dectalk.dll");

	MMRESULT (*TextToSpeechShutdown)(LPTTS_HANDLE_T);
	TextToSpeechShutdown = (MMRESULT (*)(LPTTS_HANDLE_T))GetProcAddress(dectalkDLL, "TextToSpeechShutdown");

	if (TextToSpeechShutdown == NULL) return TTS_SHUTDOWN_FUNC_NULL;
	
	status = TextToSpeechShutdown(globalTTSRef);
	return status;
}

