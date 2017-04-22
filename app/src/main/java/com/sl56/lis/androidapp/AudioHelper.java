package com.sl56.lis.androidapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;

import java.io.IOException;

/**
 * Created by Josion on 2017/2/28.
 */

public class AudioHelper {

    /**
     * 使用SoudPool播放提示音
     * @param context 播放界面
     * @param id 音频id
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void playAudioBySP(Context context, final int id){
        final SoundPool sp = new  SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        sp.release();
        final int playId = sp.load(context,id,1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sp.play(playId,1,1,0,0,1);
            }
        },2000);
    }
    /**
     * 使用MediaPlay播放提示音
     * @param context 播放提示音的界面
     * @param id 播放的音频文件id
     */
    public static void playAudioByMP(Context context,int id){
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //当播放完毕一次后，重新指向流文件的开头，以准备下次播放。
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer player) {
                        player.release();
                    }
                });
        AssetFileDescriptor file = context.getResources().openRawResourceFd(id);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(),
                    file.getStartOffset(), file.getLength());
            file.close();
            //mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            mediaPlayer = null;
        }
        mediaPlayer.start();
    }

}
