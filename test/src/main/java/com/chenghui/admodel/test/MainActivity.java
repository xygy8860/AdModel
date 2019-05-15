package com.chenghui.admodel.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.chenghui.lib.admodle.AdBannerUtils;
import com.chenghui.lib.admodle.AdInstalUtils;
import com.chenghui.lib.admodle.AdModelUtils;

public class MainActivity extends AppCompatActivity {

    private AdInstalUtils adInstalUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*FragmentManager ft = getSupportFragmentManager();
        FragmentTransaction tr = ft.beginTransaction();
        tr.replace(R.id.fragment, AdFragment.getAdFragment(false));
        tr.commitAllowingStateLoss();
        ft.executePendingTransactions();

        findViewById(R.id.hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adInstalUtils != null) {
                    adInstalUtils.ondetory();
                    adInstalUtils = null;
                }

                if (adInstalUtils == null) {
                    adInstalUtils = new AdInstalUtils(MainActivity.this, AdModelUtils.NativeId_Img, 0, null);
                }
                adInstalUtils.refreshAd(0);
            }
        });*/

        AdBannerUtils.initAd(this);
        ViewGroup layout = findViewById(R.id.adlayout);
        AdBannerUtils.initBanner(layout, layout, this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adInstalUtils != null) {
            adInstalUtils.ondetory();
        }
    }
}
