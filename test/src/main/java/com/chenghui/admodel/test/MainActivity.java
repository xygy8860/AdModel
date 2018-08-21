package com.chenghui.admodel.test;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chenghui.lib.admodle.AdInstalUtils;
import com.chenghui.lib.admodle.AdModelUtils;

public class MainActivity extends AppCompatActivity {

    private AdInstalUtils adInstalUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AdModelUtils.APPID = "1105735729";

        AdModelUtils.NativeId_Img = "1070134109570553"; // 纯图片 竖图  7010232119579486
        AdModelUtils.NativeId_Horizontal_Img = "6020535200535013";

        FragmentManager ft = getSupportFragmentManager();
        FragmentTransaction tr = ft.beginTransaction();
        tr.replace(R.id.fragment, AdFragment.getAdFragment(false));
        tr.commitAllowingStateLoss();
        ft.executePendingTransactions();

        findViewById(R.id.hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adInstalUtils == null) {
                    adInstalUtils = new AdInstalUtils(MainActivity.this,AdModelUtils.NativeId_Img,0,null);
                }
                adInstalUtils.refreshAd(0);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adInstalUtils != null) {
            adInstalUtils.ondetory();
        }
    }
}
