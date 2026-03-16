package com.example.ezya;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.View;

public class SoundManager {

    private static SoundManager instance;
    private SoundPool soundPool;
    private int soundTap;
    private int soundSuccess;
    private int soundDelete;
    private boolean loaded = false;

    private SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build();

        soundTap = soundPool.load(context, R.raw.tap, 1);
        soundSuccess = soundPool.load(context, R.raw.success, 1);
        soundDelete = soundPool.load(context, R.raw.delete, 1);

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> loaded = true);
    }

    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    public void playTap() {
        if (loaded) soundPool.play(soundTap, 1f, 1f, 1, 0, 1f);
    }

    public void playSuccess() {
        if (loaded) soundPool.play(soundSuccess, 1f, 1f, 1, 0, 1f);
    }

    public void playDelete() {
        if (loaded) soundPool.play(soundDelete, 1f, 1f, 1, 0, 1f);
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }
}