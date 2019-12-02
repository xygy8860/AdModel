package com.chenghui.lib.admodle;

import android.app.Activity;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by cdsunqinwei on 2018/3/21.
 */

public class AdInstalUtils implements NativeExpressAD.NativeExpressADListener {

    private static final String TAG = "123";

    private NativeExpressAD nativeExpressAD;
    private NativeExpressADView nativeExpressADView;
    private int count;
    private InstlDialog dialog;
    private Activity activity;
    private InterstitialAD iad;

    private String nativeId;
    private boolean isShowClosedBtn;
    private int mRand = 0;

    private boolean isVertical;
    private InstalCarouselDialog mCarouselDialog;
    private OnLoadAdListener listener;
    private TTAdNative mTTAdNative;

    // 横屏
    public AdInstalUtils(Activity activity, int mRand, OnLoadAdListener listener) {
        this(activity);
        this.mRand = mRand;
        this.listener = listener;
    }

    //竖屏
    public AdInstalUtils(Activity activity, String nativeId, int mRand, OnLoadAdListener listener) {
        this.activity = activity;
        this.nativeId = nativeId;
        this.listener = listener;
        this.mRand = mRand;
        isVertical = true;

        if (nativeId.equals(AdModelUtils.NativeId_Img) || nativeId.equals(AdModelUtils.NativeId_Horizontal_Img)) {
            isShowClosedBtn = true;
        }
    }

    private AdInstalUtils(Activity activity) {
        this.activity = activity;

        ArrayList<String> list = new ArrayList<>();
        /*if (!TextUtils.isEmpty(AdModelUtils.NativeId_Img) && !"1".equals(AdModelUtils.NativeId_Img)) {
            list.add(AdModelUtils.NativeId_Img);
        }*/
        if (!TextUtils.isEmpty(AdModelUtils.NativeId_img_txt) && !"1".equals(AdModelUtils.NativeId_img_txt)) {
            list.add(AdModelUtils.NativeId_img_txt);
        }
        if (!TextUtils.isEmpty(AdModelUtils.NativeId_txt_img) && !"1".equals(AdModelUtils.NativeId_txt_img)) {
            list.add(AdModelUtils.NativeId_txt_img);
        }
        if (!TextUtils.isEmpty(AdModelUtils.NativeId_Horizontal_Img) && !"1".equals(AdModelUtils.NativeId_Horizontal_Img)) {
            list.add(AdModelUtils.NativeId_Horizontal_Img);
        }
        int i = new Random().nextInt(list.size());
        nativeId = list.get(i);

        if (nativeId.equals(AdModelUtils.NativeId_Img) || nativeId.equals(AdModelUtils.NativeId_Horizontal_Img)) {
            isShowClosedBtn = true;
        }
    }


    public void refreshAd(int count) {
        Random random = new Random();
        int rand = random.nextInt(100);
        //没有权限
        if (!AdModelUtils.isHavePermissions(activity) || rand < AdModelUtils.TT_Native_rate) {  //如果落在头条范围内，开屏头条 rand < 50
            refreshTTAd(0);
        } else {
            refreshGdtAd(0);
        }
    }

    // 加载TT广告
    private void refreshTTAd(int count) {
        this.count = count;
        mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        refreshNativieTTAd();
    }

    private void refreshGdtAd(int count) {
        this.count = count;

        try {
            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            nativeExpressAD = new NativeExpressAD(activity, new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT), AdModelUtils.APPID,
                    nativeId, this); // 这里的Context必须为Activity
            nativeExpressAD.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS) // 设置什么网络环境下可以自动播放视频
                    .setAutoPlayMuted(true) // 设置自动播放视频时，是否静音
                    .build()); // setVideoOption是可选的，开发者可根据需要选择是否配置
            nativeExpressAD.loadAD(isVertical ? 3 : 1);
        } catch (Exception e) {
            showGdtInshal();
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        Log.i(TAG, String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));

        if (count < 2) {
            refreshGdtAd(count + 1);
        } else {
            showGdtInshal();
        }
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        try {
            if (isVertical) {
                if (mCarouselDialog == null) {
                    mCarouselDialog = new InstalCarouselDialog(activity, isShowClosedBtn, mRand, listener);
                }
                mCarouselDialog.show();
                mCarouselDialog.setAdList(adList);
            } else {
                //Log.i(TAG, "onADLoaded: " + adList.size());
                // 释放前一个展示的NativeExpressADView的资源
                if (nativeExpressADView != null) {
                    nativeExpressADView.destroy();
                }

                if (dialog == null) {
                    dialog = new InstlDialog(activity, isShowClosedBtn, mRand, listener);
                }

                dialog.show();

                nativeExpressADView = adList.get(0);
                // 广告可见才会产生曝光，否则将无法产生收益。
                dialog.setNativeAd(nativeExpressADView);
                nativeExpressADView.render();
            }

            if (listener != null) {
                listener.successed();
            }
        } catch (Exception e) {
            showGdtInshal();
        }
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail");
        if (!isVertical) {
            if (dialog != null) {
                dialog.dismiss();
            }
            showGdtInshal();
        }
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess");
        if (!isVertical) {
            if (dialog != null) {
                dialog.show();
            }
        }
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        Log.i(TAG, "onADExposure");
        if (isVertical && mCarouselDialog != null) {
            mCarouselDialog.dismissOnly();
            mCarouselDialog.show();
            mCarouselDialog.onADExposure();
        } else if (dialog != null) {
            dialog.dismissOnly();
            dialog.show();
        }
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        Log.i(TAG, "onADClicked");

        if (isVertical) {
            if (mCarouselDialog != null) {
                mCarouselDialog.dismiss();
            }
        } else {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        //Log.i(TAG, "onADClosed");
        // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，释放资源，不可以再用来展示。
        if (dialog != null) {
            dialog.dismiss();
        }

        if (mCarouselDialog != null) {
            mCarouselDialog.dismiss();
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        //Log.i(TAG, "onADLeftApplication");
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADOpenOverlay");
    }

    @Override
    public void onADCloseOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADCloseOverlay");
    }

    private void requestGdt() {
        if (iad == null) {
            iad = new InterstitialAD(activity, AdModelUtils.APPID, AdModelUtils.InstalPosID);
        }
        iad.setADListener(new AbstractInterstitialADListener() {
            @Override
            public void onADReceive() {
                iad.show();

                if (listener != null) {
                    listener.successed();
                }
            }

            @Override
            public void onNoAD(AdError adError) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adviewInstal();
                    }
                });
            }
        });
        iad.loadAD();
    }

    private void showGdtInshal() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            requestGdt();
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    requestGdt();
                }
            });
        }
    }

    private void adviewInstal() {
    }

    public void ondetory() {
        if (dialog != null) {
            dialog.dismiss();
        }

        if (mCarouselDialog != null) {
            mCarouselDialog.dismiss();
        }

        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }
    }

    public interface OnLoadAdListener {
        void successed();

        void failed();

        void closed();
    }

    // 加载个性化模板广告
    private void refreshNativieTTAd() {

        Random random = new Random();
        int rand = random.nextInt(100);
        String id = AdModelUtils.TT_native_id;
        if (rand > AdModelUtils.TT_Native_model_rate) {
            id = AdModelUtils.TT_native_video_id;
        }

        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(id) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(350, 0) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(640, 320)//这个参数设置即可，不影响模板广告的size
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                TTNativeExpressAd mTTAd = ads.get(0);
                bindAdListener(mTTAd);
                mTTAd.render();
            }
        });
    }

    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
                Log.e("123", "广告被点击");
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onAdShow(View view, int type) {
                //TToast.show(mContext, "广告展示");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                //Log.e("ExpressView","render fail:"+(System.currentTimeMillis() - startTime));
                //TToast.show(mContext, msg+" code:"+code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                if (dialog == null) {
                    dialog = new InstlDialog(activity, isShowClosedBtn, mRand, listener);
                }

                dialog.show();
                dialog.setNativeAd(view);
            }
        });
        //dislike设置
        //bindDislike(ad, false);
    }

}
