package com.example.ezya;

import android.content.Context;

public class SoundManager {

    private static SoundManager instance;

    private SoundManager(Context context) {}

    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    public void playTap() {}
    public void playSuccess() {}
    public void playDelete() {}
    public void release() { instance = null; }
}