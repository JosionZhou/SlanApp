package com.sl56.lis.androidapp;

import android.app.Application;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceAlignmentEnum;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListener;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar mActionBar = getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View actionBar = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView mTitleTextView = (TextView) actionBar.findViewById(R.id.title_text);
        mTitleTextView.setText(R.string.app_name);
        mActionBar.setCustomView(actionBar);
        mActionBar.setDisplayShowCustomEnabled(true);
        ((Toolbar) actionBar.getParent()).setContentInsetsAbsolute(0,0);

        BoomMenuButton leftBmb = (BoomMenuButton) actionBar.findViewById(R.id.action_bar_left_bmb);
        leftBmb.setButtonPlaceAlignmentEnum(ButtonPlaceAlignmentEnum.Left);
        leftBmb.setButtonLeftMargin(10);
        leftBmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                Intent it=null;
                switch (index){
                    case 0:
                        it = new Intent(MainActivity.this,CheckGoodsActivity.class);
                        break;
                    case 1:
                        it = new Intent(MainActivity.this,PutawayActivity.class);
                        break;
                    case 2:
                        it = new Intent(MainActivity.this,PrintLabelActivity.class);
                        break;
                    case 3:
                        it = new Intent(MainActivity.this,PalletActivity.class);
                        break;
                    case 4:
                        it = new Intent(MainActivity.this,PalletizedActivity.class);

                        break;

                    default:return;
                }
                if(it!=null)
                    MainActivity.this.startActivity(it);
            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

            }
        });
        BoomMenuButton rightBmb = (BoomMenuButton) actionBar.findViewById(R.id.action_bar_right_bmb);
        rightBmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                Intent it =null;
                switch(index){
                    case 0:
                        it = new Intent(MainActivity.this,ClearanceActivity.class);

                        break;
                    case 1:
                        it = new Intent(MainActivity.this,ArchiveScanActivity.class);

                        break;
                    case 2:
                        it = new Intent(MainActivity.this,RemovePallet.class);

                        break;
                    case 3:
                        it = new Intent(MainActivity.this,StationMemberSettingActivity.class);
                        break;
                    case 4:
                        System.exit(0);
                        break;
                }
                if(it!=null)
                    MainActivity.this.startActivity(it);
            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

            }
        });
        rightBmb.setButtonEnum(ButtonEnum.Ham);
        rightBmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_5);
        rightBmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_5);
        leftBmb.setButtonEnum(ButtonEnum.Ham);
        leftBmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_5);
        leftBmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_5);
        for (int i = 0; i < leftBmb.getPiecePlaceEnum().pieceNumber(); i++){
            HamButton.Builder builder = BuilderManager.getSlanHamButtonBuilder();
            leftBmb.addBuilder(builder);
        }
        for (int i = 0; i < rightBmb.getPiecePlaceEnum().pieceNumber(); i++){
            HamButton.Builder builder = BuilderManager.getSlanHamButtonBuilder();
            rightBmb.addBuilder(builder);
        }

        findViewById(R.id.btnScanRgd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this,ScanRGDActivity.class);
                startActivity(it);
            }
        });

        findViewById(R.id.btn_PackagingOptimization).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this,PackagingOptimizationActivity.class);
                startActivity(it);
            }
        });

        findViewById(R.id.btn_PrintInnerLabel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this,PrintInnerLabelActivity.class);
                startActivity(it);
            }
        });
        findViewById(R.id.btn_bind_FBA).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this,BindFBAActivity.class);
                startActivity(it);
            }
        });
//        rightBmb.setButtonEnum(ButtonEnum.Ham);
//        rightBmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_4);
//        rightBmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_4);
//        for (int i = 0; i < rightBmb.getPiecePlaceEnum().pieceNumber(); i++)
//            rightBmb.addBuilder(BuilderManager.getSlanHamButtonBuilder());
    }
}
