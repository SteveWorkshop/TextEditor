package io.github.materialapps.texteditor.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * 一个小彩蛋
 * 柚子厨蒸鹅心（bushi
 */

public class SoundUtil {
    private static final String TAG = "SoundUtil";
    private static MediaPlayer mediaPlayer;

    public static void init(Context context){
        if(mediaPlayer==null){
            try {
                mediaPlayer=new MediaPlayer();
                AssetManager assets = context.getAssets();
                AssetFileDescriptor assetFileDescriptor = assets.openFd("ciallo.mp3");
                mediaPlayer.setDataSource(assetFileDescriptor);
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e(TAG, "init: ", e);
            }
        }

    }

    public static void makeCiallo(){
        if(mediaPlayer!=null){
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }
    }
}
