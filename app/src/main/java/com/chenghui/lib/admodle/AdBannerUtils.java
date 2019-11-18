package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by 孙勤伟 on 2017/2/6.
 */
public class AdBannerUtils {

    private static UnifiedBannerView bv;
    private static List<TTNativeExpressAd> mTTAdList = new ArrayList<>();
    private static TTAdNative mTTAdNative;


    public static void initBanner(ViewGroup ttLayout, ViewGroup gdt, Activity context) {

        Random random = new Random();
        int rand = random.nextInt(100);
        if (!AdModelUtils.isHavePermissions(context) || rand < AdModelUtils.TT_Banner_rate) {
            loadExpressAd(ttLayout, gdt, context);
        } else {
            gdtBanner(gdt, context);
        }
    }

    /**
     * 广点通 banner 广告
     *
     * @param gdtLayout
     * @param activity
     */
    private static void gdtBanner(final ViewGroup gdtLayout, final Activity activity) {
        try {
            // 清除加载的广告
            if (!isNetworkAvailable(activity) || gdtLayout == null) {
                return;
            }
            gdtLayout.removeAllViews();

            bv = new UnifiedBannerView(activity, AdModelUtils.APPID, AdModelUtils.BannerPosID_2, new UnifiedBannerADListener() {
                @Override
                public void onNoAD(AdError adError) {

                }

                @Override
                public void onADReceive() {

                }

                @Override
                public void onADExposure() {

                }

                @Override
                public void onADClosed() {
                    gdtLayout.removeAllViews();
                    if (bv != null) {
                        bv.destroy();
                    }
                }

                @Override
                public void onADClicked() {

                }

                @Override
                public void onADLeftApplication() {

                }

                @Override
                public void onADOpenOverlay() {

                }

                @Override
                public void onADCloseOverlay() {

                }
            });
            gdtLayout.addView(bv);
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

    private static void loadExpressAd(final ViewGroup ttLayout, final ViewGroup gdtLayout, final Activity activity) {
        ttLayout.removeAllViews();

        final ViewGroup bannerContainer = new FrameLayout(activity);
        ttLayout.addView(bannerContainer);


        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(AdModelUtils.TT_Banner_id) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(getWidth4Dp(activity), 55) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(600, 150)
                .build();
        mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                ttLayout.removeAllViews();
                //gdtBanner(gdtLayout, activity);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                TTNativeExpressAd mTTAd = ads.get(0);
                mTTAdList.add(mTTAd);
                mTTAd.setSlideIntervalTime(30 * 1000);
                bindAdListener(mTTAd, bannerContainer, activity);
                //startTime = System.currentTimeMillis();
                mTTAd.render();
            }
        });
    }

    private static void bindAdListener(TTNativeExpressAd ad, final ViewGroup bannerContainer, Activity activity) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
            }

            @Override
            public void onAdShow(View view, int type) {
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                bannerContainer.removeAllViews();
                bannerContainer.addView(view);
            }
        });
        //dislike设置
        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                //用户选择不喜欢原因后，移除广告展示
                bannerContainer.removeAllViews();
            }

            @Override
            public void onCancel() {
                //TToast.show(mContext, "点击取消 ");
            }
        });
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {

            }

            @Override
            public void onDownloadActive(long l, long l1, String s, String s1) {

            }

            @Override
            public void onDownloadPaused(long l, long l1, String s, String s1) {

            }

            @Override
            public void onDownloadFailed(long l, long l1, String s, String s1) {

            }

            @Override
            public void onDownloadFinished(long l, String s, String s1) {

            }

            @Override
            public void onInstalled(String s, String s1) {

            }
        });
    }


    /**
     * px 转换成 dp
     *
     * @param context 上下文对象
     * @return
     */
    public static int getWidth4Dp(Activity context) {
        int screenWidth = context.getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：480px）
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (screenWidth / scale + 0.5f);
    }


}
