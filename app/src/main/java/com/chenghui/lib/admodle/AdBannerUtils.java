package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chenghui.study.sdk.InitConfiguration;
import com.chenghui.study.sdk.interfaces.AdViewBannerListener;
import com.chenghui.study.sdk.manager.AdViewBannerManager;
import com.chenghui.study.sdk.manager.AdViewInstlManager;
import com.chenghui.study.sdk.manager.AdViewSpreadManager;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.BannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.comm.util.AdError;

/**
 * Created by 孙勤伟 on 2017/2/6.
 */
public class AdBannerUtils {

    private static BannerView bv;

    public static void initAd(Context context) {
        if (TextUtils.isEmpty(AdModelUtils.SDK_KEY)) {
            return;
        }

        try {
            InitConfiguration initConfig = new InitConfiguration.Builder(context).setUpdateMode(InitConfiguration.UpdateMode.EVERYTIME)
                    // 实时获取配置，非必须写
                    .setAdSize(InitConfiguration.AdSize.BANNER_SMART)
                    .setBannerCloseble(InitConfiguration.BannerSwitcher.CANCLOSED) //横幅可关闭按钮 .setRunMode(RunMode.TEST) //测试模式时log更多，方便调试，不影响竞价展示正式广告和收益情况，可能影响个别第三方平台的正式广告展示，建议应用上线时删除该行
                    .build(); // 初始化横幅、插屏、原生、开屏、视频的广告配置，必须写
            AdViewBannerManager.getInstance(context).init(initConfig, new String[]{AdModelUtils.SDK_KEY});
            AdViewInstlManager.getInstance(context).init(initConfig, new String[]{AdModelUtils.SDK_KEY});
            //AdViewNativeManager.getInstance(this).init(initConfig,MainActivity.keySet);
            AdViewSpreadManager.getInstance(context).init(initConfig, new String[]{AdModelUtils.SDK_KEY});
            //AdViewVideoManager.getInstance(this).init(initConfig,MainActivity.keySet);
        } catch (Throwable e) {

        }
    }

    public static void initBanner(ViewGroup layout, ViewGroup gdt, Activity context) {

        try {
            initAd(context);
            adviewBanner(layout, gdt, context, null);
        } catch (Throwable e) {

        }

        gdtBanner(gdt, layout, context);
    }

    // region 3.2.3 自定义插屏代码
    private static void adviewBanner(final ViewGroup adviewLayout, final ViewGroup mGdtLayout, final Activity context, final ImageView img) {
        try {
            if (!isNetworkAvailable(context) || adviewLayout == null) {
                return;
            }

            adviewLayout.removeAllViews();
            View view = AdViewBannerManager.getInstance(context).getAdViewLayout(context, AdModelUtils.SDK_KEY);
            if (null != view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeAllViews();
                }
            }
            AdViewBannerManager.getInstance(context).requestAd(context, AdModelUtils.SDK_KEY, new AdViewBannerListener() {
                @Override
                public void onAdClick(String s) {
                }

                @Override
                public void onAdDisplay(String s) {
                    mGdtLayout.removeAllViews();
                    if (bv != null) {
                        bv.destroy();
                    }

                    if (img != null && !img.isShown()) {
                        img.setVisibility(View.VISIBLE);
                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (null != adviewLayout) {
                                    adviewLayout.removeView(adviewLayout.findViewWithTag(AdModelUtils.SDK_KEY));
                                    img.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onAdClose(String s) {
                    if (null != adviewLayout) {
                        adviewLayout.removeView(adviewLayout.findViewWithTag(AdModelUtils.SDK_KEY));
                    }
                }

                @Override
                public void onAdFailed(String s) {
                    //gdtBanner(mGdtLayout, context);
                }

                @Override
                public void onAdReady(String s) {

                }
            });
            view.setTag(AdModelUtils.SDK_KEY);
            adviewLayout.addView(view);
            adviewLayout.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // endregion


    /**
     * 广点通 banner 广告
     *
     * @param bannerContainer
     * @param activity
     */
    private static void gdtBanner(final ViewGroup bannerContainer, final ViewGroup adviewLayout, final Activity activity) {
        try {
            // 清除加载的广告
            if (!isNetworkAvailable(activity) || bannerContainer == null) {
                return;
            }
            bannerContainer.removeAllViews();

            bv = new BannerView(activity, ADSize.BANNER, AdModelUtils.APPID, AdModelUtils.BannerPosID);
            bv.setShowClose(true);
            bv.setADListener(new BannerADListener() {

                @Override
                public void onNoAD(AdError adError) {
                    adviewBanner(adviewLayout, bannerContainer, activity, null);
                }

                @Override
                public void onADReceiv() {
                }

                @Override
                public void onADOpenOverlay() {
                }

                @Override
                public void onADLeftApplication() {
                }

                @Override
                public void onADExposure() {

                }

                @Override
                public void onADClosed() {
                    bannerContainer.removeAllViews();
                    if (bv != null) {
                        bv.destroy();
                    }
                }

                @Override
                public void onADCloseOverlay() {
                }

                @Override
                public void onADClicked() {

                }
            });
            bannerContainer.addView(bv);
            bv.loadAD();
        } catch (Exception e) {
            // 异步线程问题
        }
    }

    /*** 是否连接网络 **/
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    // 当前网络是连接的
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        // 当前所连接的网络可用
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }


}
