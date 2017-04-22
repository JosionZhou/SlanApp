package com.sl56.lis.androidapp;

import android.content.Context;
import android.os.Vibrator;


/**
 * Created by Josion on 2017/3/3.
 * 震动类
 */

public class VibratorHelper {

    /**
     * 使机器震动，默认时间400毫秒
     * @param context
     */
    public static void shock(Context context){

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0,400},-1);
    }

    /**
     *
     * @param context
     * @param shockTime 震动持续时间
     */
    public static void shock(Context context,int shockTime){

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0,shockTime},-1);
    }
}
