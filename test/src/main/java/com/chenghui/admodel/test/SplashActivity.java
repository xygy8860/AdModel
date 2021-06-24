package com.chenghui.admodel.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chenghui.lib.admodle.AdModelUtils;
import com.chenghui.lib.admodle.SplashBaseActivity;

public class SplashActivity extends SplashBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mJumpBtn = findViewById(R.id.jump_btn);
        splashLayout = findViewById(R.id.splashview);

        AdModelUtils.APPID = "1105735729";
        AdModelUtils.SDK_KEY = "SDK20171309010545uakpnj1pd5o551c";

        AdModelUtils.NativeId_Img = "1070134109570553"; // 纯图片 竖图  7010232119579486
        AdModelUtils.NativeId_Horizontal_Img = "6020535200535013";
        AdModelUtils.BannerPosID = "8000811616180200";

        AdModelUtils.BD_Appid = "d74c40aa";
        AdModelUtils.BD_Banner_id = "6209488";
        AdModelUtils.BD_Banner_rate = 100;

        AdModelUtils.TT_Appid = "5017050";
        AdModelUtils.TT_Splash_id = "817050394";
        AdModelUtils.TT_Name = "C语言学习宝典";

        //initAdParams();

        openMainActivity();
    }


    @Override
    protected void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
