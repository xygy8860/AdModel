package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.ViewGroup;

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

        //InitSDKManager.getInstance().init(context, AdModelUtils.SDK_KEY);
    }

    public static void initBanner(ViewGroup layout, ViewGroup gdt, Activity context) {

        /*try {
            initAd(context);
            adviewBanner(layout, gdt, context, null);
        } catch (Throwable e) {

        }*/

        gdtBanner(gdt, layout, context);
    }

    // region 3.2.3 自定义插屏代码
    /*private static void adviewBanner(final ViewGroup adviewLayout, final ViewGroup mGdtLayout, final Activity context, final ImageView img) {
        try {
            if (!isNetworkAvailable(context) || adviewLayout == null) {
                return;
            }

            adviewLayout.removeAllViews();

            AdViewBannerManager adViewBIDView = new AdViewBannerManager(context,
                    AdModelUtils.SDK_KEY, AdViewBannerManager.BANNER_AUTO_FILL, true);
//		adViewBIDView.logMode=false;
            adViewBIDView.setShowCloseBtn(true);
            adViewBIDView.setRefreshTime(15);
            adViewBIDView.setOpenAnim(true);
            adViewBIDView.setOnAdViewListener(new AdViewBannerListener() {
                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdDisplayed() {
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
                public void onAdReceived() {

                }

                @Override
                public void onAdFailedReceived(String s) {

                }

                @Override
                public void onAdClosed() {
                    if (null != adviewLayout) {
                        adviewLayout.removeView(adviewLayout.findViewWithTag(AdModelUtils.SDK_KEY));
                    }
                }
            });

            View view = adViewBIDView.getAdViewLayout();
            if (null != view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeAllViews();
                }
            }

            adviewLayout.addView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

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
                    //adviewBanner(adviewLayout, bannerContainer, activity, null);
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
