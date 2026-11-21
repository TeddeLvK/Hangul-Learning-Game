#include <jni.h>
#include "engine.h"

static EngineState g_state;

JNIEXPORT void JNICALL Java_com_koreangame_NativeEngine_nativeInit(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    engine_init(&g_state);
}

JNIEXPORT void JNICALL Java_com_koreangame_NativeEngine_nativeLoadMap(
    JNIEnv *env, jclass cls, jstring flatTiles, jfloat spawnX, jfloat spawnY
) {
    (void)cls;
    const char *utf = (*env)->GetStringUTFChars(env, flatTiles, 0);
    if (utf) {
        engine_load_map(&g_state, utf, spawnX, spawnY);
        (*env)->ReleaseStringUTFChars(env, flatTiles, utf);
    }
}

JNIEXPORT void JNICALL Java_com_koreangame_NativeEngine_nativeClearEntities(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    engine_clear_entities(&g_state);
}

JNIEXPORT void JNICALL Java_com_koreangame_NativeEngine_nativeAddEntity(
    JNIEnv *env, jclass cls, jint tx, jint ty
) {
    (void)env;
    (void)cls;
    engine_add_entity(&g_state, (int)tx, (int)ty);
}

JNIEXPORT void JNICALL Java_com_koreangame_NativeEngine_nativeSetPlayerPos(
    JNIEnv *env, jclass cls, jfloat px, jfloat py
) {
    (void)env;
    (void)cls;
    engine_set_player_pos(&g_state, (float)px, (float)py);
}

JNIEXPORT void JNICALL Java_com_koreangame_NativeEngine_nativeUpdate(
    JNIEnv *env, jclass cls, jfloat dt, jboolean up, jboolean down, jboolean left, jboolean right
) {
    (void)env;
    (void)cls;
    engine_update(&g_state, (float)dt, up ? 1 : 0, down ? 1 : 0, left ? 1 : 0, right ? 1 : 0);
}

JNIEXPORT jfloat JNICALL Java_com_koreangame_NativeEngine_nativeGetPlayerX(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    return g_state.px;
}

JNIEXPORT jfloat JNICALL Java_com_koreangame_NativeEngine_nativeGetPlayerY(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    return g_state.py;
}

JNIEXPORT jint JNICALL Java_com_koreangame_NativeEngine_nativeGetNearbyEntity(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    return g_state.nearby_entity;
}

JNIEXPORT jint JNICALL Java_com_koreangame_NativeEngine_nativePickEntity(
    JNIEnv *env, jclass cls, jfloat wx, jfloat wy
) {
    (void)env;
    (void)cls;
    return engine_pick_entity(&g_state, (float)wx, (float)wy);
}

JNIEXPORT jfloat JNICALL Java_com_koreangame_NativeEngine_nativeGetAnimTime(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;
    return g_state.anim_time;
}
