package com.sl56.lis.androidapp;

import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;

/**
 * Created by Weiping Huang at 23:44 on 16/11/21
 * For Personal Open Source
 * Contact me at 2584541288@qq.com or nightonke@outlook.com
 * For more projects: https://github.com/Nightonke
 */
public class BuilderManager {

    private static int[] imageResources = new int[]{
            R.drawable.chahuo,
            R.drawable.dayin,
            R.drawable.daban,
            R.drawable.qingcang,
            R.drawable.sitescan,
            R.drawable.sitescan,
            R.drawable.about,
            R.drawable.exit
    };
    private static int[] textResources=new int[]{
            R.string.text_slan_action_checkgoods,
            R.string.text_slan_action_printlabel,
            R.string.text_slan_action_pallet,
            R.string.text_slan_action_qingcang,
            R.string.text_slan_action_tosite,
            R.string.text_slan_action_fromsite,
            R.string.about,
            R.string.exit
    };
    private static int[] subTextResources=new int[]{
            R.string.text_slan_action_chahuo_sub,
            R.string.text_slan_action_dayin_sub,
            R.string.text_slan_action_daban_sub,
            R.string.text_slan_action_qingcang_sub,
            R.string.text_slan_action_tosite_sub,
            R.string.text_slan_action_fromsite_sub,
            R.string.about_sub,
            R.string.exit_sub
    };
    private static int[] colorResources=new int[]{
            R.color.chahuo,
            R.color.dayin,
            R.color.daban,
            R.color.qingcang,
            R.color.blue,
            R.color.purple,
            R.color.firebrick,
            R.color.green
    };
    private static int imageResourceIndex = 0;
    private static int textResourceIndex = 0;
    private static int  colorResourceIndex=0;
    private static int subTextResourceIndex=0;

    static int getImageResource() {
        if (imageResourceIndex >= imageResources.length) imageResourceIndex = 0;
        return imageResources[imageResourceIndex++];
    }
    static int getTextResource() {
        if (textResourceIndex >= textResources.length) textResourceIndex = 0;
        return textResources[textResourceIndex++];
    }
    static int getSubTextResource() {
        if (subTextResourceIndex >= subTextResources.length) subTextResourceIndex = 0;
        return subTextResources[subTextResourceIndex++];
    }
    static int getColorResource(){
        if (colorResourceIndex >= colorResources.length) colorResourceIndex = 0;
        return colorResources[colorResourceIndex++];
    }
    static HamButton.Builder  getSlanHamButtonBuilder() {
        return new HamButton.Builder()
                .normalImageRes(getImageResource())
                .normalTextRes(getTextResource())
                .subNormalTextRes(getSubTextResource())
                .normalColorRes(getColorResource());
    }

    static SimpleCircleButton.Builder getSimpleCircleButtonBuilder() {
        return new SimpleCircleButton.Builder()
                .normalImageRes(getImageResource());
    }

//    static TextInsideCircleButton.Builder getTextInsideCircleButtonBuilder() {
//        return new TextInsideCircleButton.Builder()
//                .normalImageRes(getImageResource())
//                .normalTextRes(R.string.text_inside_circle_button_text_normal);
//    }
//
//    static TextInsideCircleButton.Builder getTextInsideCircleButtonBuilderWithDifferentPieceColor() {
//        return new TextInsideCircleButton.Builder()
//                .normalImageRes(getImageResource())
//                .normalTextRes(R.string.text_inside_circle_button_text_normal)
//                .pieceColor(Color.WHITE);
//    }
//
//    static TextOutsideCircleButton.Builder getTextOutsideCircleButtonBuilder() {
//        return new TextOutsideCircleButton.Builder()
//                .normalImageRes(getImageResource())
//                .normalTextRes(R.string.text_outside_circle_button_text_normal);
//    }
//
//    static TextOutsideCircleButton.Builder getTextOutsideCircleButtonBuilderWithDifferentPieceColor() {
//        return new TextOutsideCircleButton.Builder()
//                .normalImageRes(getImageResource())
//                .normalTextRes(R.string.text_outside_circle_button_text_normal)
//                .pieceColor(Color.WHITE);
//    }
//
//    static HamButton.Builder getHamButtonBuilder() {
//        return new HamButton.Builder()
//                .normalImageRes(getImageResource())
//                .normalTextRes(R.string.text_ham_button_text_normal)
//                .subNormalTextRes(R.string.text_ham_button_sub_text_normal);
//    }
//
//    static HamButton.Builder getHamButtonBuilderWithDifferentPieceColor() {
//        return new HamButton.Builder()
//                .normalImageRes(getImageResource())
//                .normalTextRes(R.string.text_ham_button_text_normal)
//                .subNormalTextRes(R.string.text_ham_button_sub_text_normal)
//                .pieceColor(Color.WHITE);
//    }

    private static BuilderManager ourInstance = new BuilderManager();

    public static BuilderManager getInstance() {
        return ourInstance;
    }

    private BuilderManager() {
    }
}
