package com.koreangame;

public final class NativeEngine {
    static {
        System.loadLibrary("koreangame");
    }

    private NativeEngine() {}

    public static native void nativeInit();
    public static native void nativeLoadMap(String flatTiles, float spawnX, float spawnY);
    public static native void nativeClearEntities();
    public static native void nativeAddEntity(int tx, int ty);
    public static native void nativeSetPlayerPos(float px, float py);
    public static native void nativeUpdate(float dt, boolean up, boolean down, boolean left, boolean right);
    public static native float nativeGetPlayerX();
    public static native float nativeGetPlayerY();
    public static native int nativeGetNearbyEntity();
    public static native int nativePickEntity(float wx, float wy);
    public static native float nativeGetAnimTime();
}
