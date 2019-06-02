package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.BannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.comm.util.AdError;

import java.util.Random;

/**
 * Created by 孙勤伟 on 2017/2/6.
 */
public class AdBannerUtils {

    private static BannerView bv;


    public static void initBanner(ViewGroup layout, ViewGroup gdt, Activity context) {

        Random random = new Random();

        int rand = random.nextInt(100);
        if (rand < AdModelUtils.TT_Banner_rate) {
            loadBannerTTAd(layout, gdt, context);
        } else {
            gdtBanner(gdt, layout, context);
        }
    }

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

    private static void loadBannerTTAd(final ViewGroup bannerContainer, final ViewGroup adviewLayout, final Activity activity) {
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(AdModelUtils.TT_Banner_id) //广告位id
                .setSupportDeepLink(true)
                .setImageAcceptedSize(600, 150)
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        mTTAdNative.loadBannerAd(adSlot, new TTAdNative.BannerAdListener() {

            @Override
            public void onError(int code, String message) {
                gdtBanner(bannerContainer, adviewLayout, activity);
            }

            @Override
            public void onBannerAdLoad(final TTBannerAd ad) {
                if (ad == null) {
                    return;
                }
                View bannerView = ad.getBannerView();
                if (bannerView == null) {
                    return;
                }
                //设置轮播的时间间隔  间隔在30s到120秒之间的值，不设置默认不轮播
                ad.setSlideIntervalTime(30 * 1000);
                bannerContainer.removeAllViews();

                FrameLayout frameLayout = new FrameLayout(activity);
                ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                frameLayout.setLayoutParams(params);
                frameLayout.addView(bannerView);

                bannerContainer.addView(frameLayout);
                //（可选）设置下载类广告的下载监听
                //bindDownloadListener(ad);
                //在banner中显示网盟提供的dislike icon，有助于广告投放精准度提升
                ad.setShowDislikeIcon(new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        //TToast.show(mContext, "点击 " + value);
                        //用户选择不喜欢原因后，移除广告展示
                        bannerContainer.removeAllViews();
                    }

                    @Override
                    public void onCancel() {
                        //TToast.show(mContext, "点击取消 ");
                    }
                });
            }
        });
    }


}
