package com.chenghui.lib.admodle;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by cdsunqinwei on 2018/3/22.
 */

public class AdNativeUtils implements NativeExpressAD.NativeExpressADListener {

    private static final String TAG = "123";

    private NativeExpressAD nativeExpressAD;
    private NativeExpressADView nativeExpressADView;
    private int count;
    private Activity activity;
    private String nativeId;
    private ViewGroup layout;

    private OnSuccessListener listener;

    public AdNativeUtils(Activity activity, ViewGroup layout) {
        this.activity = activity;
        this.layout = layout;
        ArrayList<String> list = new ArrayList<>();
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
        refreshAllAd(0);
    }

    public AdNativeUtils(Activity activity, ViewGroup layout, OnSuccessListener listener) {
        this.activity = activity;
        this.layout = layout;
        this.listener = listener;

        nativeId = AdModelUtils.NativeId_Horizontal_Img;
        refreshAllAd(0);
    }

    private void refreshAllAd(int count) {
        //没有权限
        if (AdModelUtils.TT_Native_rate == 0) {  //如果落在头条范围内，开屏头条 rand < 50
            refreshAd(0);
        }
    }

    private void refreshAd(int count) {
        this.count = count;

        try {
            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            nativeExpressAD = new NativeExpressAD(activity, new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT),
                    nativeId, this); // 这里的Context必须为Activity
            nativeExpressAD.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS) // 设置什么网络环境下可以自动播放视频
                    .setAutoPlayMuted(true) // 设置自动播放视频时，是否静音
                    .build()); // setVideoOption是可选的，开发者可根据需要选择是否配置
            nativeExpressAD.loadAD(1);
        } catch (Exception e) {

        }
    }

    @Override
    public void onNoAD(AdError adError) {
        //Log.i(TAG, String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));
        //refreshTTAd(2);
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        try {
            //Log.i(TAG, "onADLoaded: " + adList.size());
            // 释放前一个展示的NativeExpressADView的资源
            if (nativeExpressADView != null) {
                nativeExpressADView.destroy();
            }

            if (layout.getVisibility() != View.VISIBLE) {
                layout.setVisibility(View.VISIBLE);
            }

            if (layout.getChildCount() > 0) {
                layout.removeAllViews();
            }

            nativeExpressADView = adList.get(0);
            if (DownloadConfirmHelper.USE_CUSTOM_DIALOG) {
                nativeExpressADView.setDownloadConfirmListener(DownloadConfirmHelper.DOWNLOAD_CONFIRM_LISTENER);
            }
            // 广告可见才会产生曝光，否则将无法产生收益。
            layout.addView(nativeExpressADView);
            nativeExpressADView.render();
        } catch (Exception e) {

        }
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail");
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess");
        if (listener != null) {
            listener.success();
        }
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        //Log.i(TAG, "onADExposure");
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        //Log.i(TAG, "onADClicked");
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        //Log.i(TAG, "onADClosed");
        // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，释放资源，不可以再用来展示。
        if (layout != null && layout.getChildCount() > 0) {
            layout.removeAllViews();
            layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        //Log.i(TAG, "onADLeftApplication");
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        //Log.i(TAG, "onADOpenOverlay");
    }

    @Override
    public void onADCloseOverlay(NativeExpressADView adView) {
        //Log.i(TAG, "onADCloseOverlay");
    }


    public void ondetory() {
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }
    }

    public interface OnSuccessListener {
        void success();
    }

    private TTAdNative mTTAdNative;

    // 加载TT广告
    private void refreshTTAd(int count) {
        this.count = count;
        mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        refreshNativieTTAd(count);
    }

    // 加载个性化模板广告
    private void refreshNativieTTAd(final int count) {
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
                if (count < 1) {
                    refreshAd(1);
                }
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
                //TToast.show(mContext, "广告被点击");
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
                layout.removeAllViews();
                layout.addView(view);
            }
        });
        //dislike设置
        bindDislike(ad);
    }

    /**
     * 设置广告的不喜欢，注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     *
     * @param ad
     */
    private void bindDislike(TTNativeExpressAd ad) {
        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int position, String value) {
                //TToast.show(mContext, "点击 " + value);
                //用户选择不喜欢原因后，移除广告展示
                if (layout != null) {
                    layout.removeAllViews();
                }
            }

            @Override
            public void onCancel() {
                //TToast.show(mContext, "点击取消 ");
            }

            @Override
            public void onRefuse() {

            }
        });
    }
}
