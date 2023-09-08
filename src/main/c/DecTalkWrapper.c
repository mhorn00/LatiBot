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
#define TTS_ENUM_LANGS_FUNC_NULL 0xF0F4
#define TTS_LANG_START_NOT_SUPPORTED 0xF011
#define TTS_LANG_START_NOT_AVAILBLE 0xF012
#define TTS_LANG_START_UNKNOWN_ERROR 0xF013
#define TTS_STARTUP_FAILED 0xF021
#define TTS_SPEAK_STRING_ERR 0xF031

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
	
	// if (TextToSpeechEnumLangs != NULL) {
	// 	TextToSpeechEnumLangs(&dt_langs);
	// } else {
	// 	return TTS_ENUM_LANGS_FUNC_NULL;
	// }
	// if (TextToSpeechStartLang != NULL) {
	// 	unsigned int TTS_lang = TextToSpeechStartLang("us");

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
	if (TextToSpeechStartup != NULL) {
		status = TextToSpeechStartup(NULL, &globalTTSRef, 0xffffffff, 0);
		return status;
	}
	return TTS_START_UNKNOWN_ERROR;
 }

 JNIEXPORT jint JNICALL Java_latibot_audio_DecTalkWrapper_TextToSeechSpeak(JNIEnv * env, jobject thisObj, jstring text) {
	//Load library
	HMODULE dectalkDLL = LoadLibrary("dectalk.dll");
	
	//define and assign library funcs
	MMRESULT (*TextToSpeechSpeak)(LPTTS_HANDLE_T, LPSTR, DWORD);
	TextToSpeechSpeak = (MMRESULT (*)(LPTTS_HANDLE_T, LPSTR, DWORD))GetProcAddress(dectalkDLL, "TextToSpeechSpeak");
	
	if (TextToSpeechSpeak == NULL) return TTS_SPEAK_FUNC_NULL;

	const char* str = (*env)->GetStringUTFChars(env,text,NULL);
	if (str == NULL) return TTS_SPEAK_STRING_ERR;
	status = TextToSpeechSpeak(globalTTSRef, str, 1);
	(*env)->ReleaseStringUTFChars(env,text,str);
	return status;
 }

