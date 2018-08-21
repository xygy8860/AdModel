package com.chenghui.lib.admodle;

import android.app.Activity;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.chenghui.study.sdk.interfaces.AdViewInstlListener;
import com.chenghui.study.sdk.manager.AdViewInstlManager;
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
            refreshAd(count + 1);
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
                mCarouselDialog.setCloseListener();
            }
        } else {
            if (dialog != null) {
                dialog.setCloseListener();
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
        AdBannerUtils.initAd(activity);
        // 初始化之后请求插屏广告，请求与展示广告，单独使用
        AdViewInstlManager.getInstance(activity).requestAd(activity, AdModelUtils.SDK_KEY, new AdViewInstlListener() {
            @Override
            public void onAdClick(String s) {

            }

            @Override
            public void onAdDisplay(String s) {

            }

            @Override
            public void onAdDismiss(String s) {

            }

            @Override
            public void onAdRecieved(String s) {
                // 请求广告成功之后，调用展示广告
                AdViewInstlManager.getInstance(activity).showAd(activity, AdModelUtils.SDK_KEY);

                if (listener != null) {
                    listener.successed();
                }
            }

            @Override
            public void onAdFailed(String s) {
                if (listener != null) {
                    listener.failed();
                }
            }
        });
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

}
