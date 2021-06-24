package com.chenghui.lib.admodle;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.qq.e.ads.cfg.VideoOption;
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

    private String nativeId;

    private boolean isVertical;
    private InstalCarouselDialog mCarouselDialog;
    private OnLoadAdListener listener;
    private TTFullScreenVideoAd mttFullVideoAd;
    private boolean mIsLoaded;
    private boolean mIsError;
    private boolean isShow;
    private boolean isTTVertical = true;

    // 横屏
    public AdInstalUtils(Activity activity, int mRand, OnLoadAdListener listener) {
        this(activity);
        this.listener = listener;
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
    }

    public void loadTTNewInstal(boolean isVertical, boolean isNeedShow) {

        this.isTTVertical = isVertical;
        if (mttFullVideoAd != null && mIsLoaded) {
            return;
        }

        mIsError = false;
        mIsLoaded = false;
        isShow = false;

        if (isNeedShow) {
            isShow = true;
        }

        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(isVertical ? AdModelUtils.TT_instal_id : AdModelUtils.TT_instal_Horizontal_id)
                //模板广告需要设置期望个性化模板广告的大小,单位dp,激励视频场景，只要设置的值大于0即可
                .setExpressViewAcceptedSize(500, 500)
                .setSupportDeepLink(true)
                .setOrientation(isVertical ? TTAdConstant.VERTICAL : TTAdConstant.HORIZONTAL)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            //请求广告失败
            @Override
            public void onError(final int code, final String message) {
                mIsLoaded = false;
                mIsError = true;

                if (isShow && AdModelUtils.TT_Native_rate != 0) {
                    refreshAd(0);
                }
            }

            //广告物料加载完成的回调
            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                mttFullVideoAd = ad;
            }

            //广告视频/图片加载完成的回调，接入方可以在这个回调后展示广告
            @Override
            public void onFullScreenVideoCached() {
                mIsLoaded = true;
                if (isShow) {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mttFullVideoAd != null && mIsLoaded) {
                                    //展示广告，并传入广告展示的场景
                                    mttFullVideoAd.showFullScreenVideoAd(activity, TTAdConstant.RitScenes.HOME_GET_PROPS, null);
                                    mttFullVideoAd = null;
                                }
                            }
                        });
                    } catch (Exception e) {

                    }
                }
            }
        });
    }


    /**
     * @param isTT 是否展示头条
     */
    public synchronized void showTTInstal(boolean isVertical, boolean isTT) {
        this.isTTVertical = isVertical;

        if (AdModelUtils.TT_Native_rate == 0 && !isTT) {  //如果落在腾讯范围内，加载腾讯
            refreshAd(0);
            return;
        }

        try {
            if (mttFullVideoAd != null && mIsLoaded) {
                //展示广告，并传入广告展示的场景
                mttFullVideoAd.showFullScreenVideoAd(activity, TTAdConstant.RitScenes.HOME_GET_PROPS, null);
                mttFullVideoAd = null;
                return;
            }
        } catch (Exception e) {

        }

        if (mIsError || mttFullVideoAd == null) {
            loadTTNewInstal(isTTVertical, true);
        } else if (!mIsLoaded) {
            isShow = true;
        }
    }

    public void refreshAd(int count) {
        refreshGdtAd(0);
    }

    private void refreshGdtAd(int count) {
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
            nativeExpressAD.loadAD(isVertical ? 3 : 1);
        } catch (Exception e) {
            if (AdModelUtils.TT_Native_rate == 0) {
                showTTInstal(isTTVertical, true);
            }
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        Log.i(TAG, String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));
        if (AdModelUtils.TT_Native_rate == 0) {
            if (count < 1) {
                refreshGdtAd(1);
            } else {
                showTTInstal(isTTVertical, true);
            }
        }
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        try {
            if (isVertical) {
                if (mCarouselDialog == null) {
                    mCarouselDialog = new InstalCarouselDialog(activity, listener);
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
                    dialog = new InstlDialog(activity, listener);
                }

                dialog.show();

                nativeExpressADView = adList.get(0);
                if (DownloadConfirmHelper.USE_CUSTOM_DIALOG) {
                    nativeExpressADView.setDownloadConfirmListener(DownloadConfirmHelper.DOWNLOAD_CONFIRM_LISTENER);
                }
                // 广告可见才会产生曝光，否则将无法产生收益。
                dialog.setNativeAd(nativeExpressADView);
                nativeExpressADView.render();

                if (AdModelUtils.TT_Native_rate != 0) {
                    int count = AdmodelSPUtls.getInstance(activity).getInt("gdtCount", 0);
                    AdmodelSPUtls.getInstance(activity).putInt("gdtCount", count + 3);
                }
            }

            if (listener != null) {
                listener.successed();
            }
        } catch (Exception e) {
            if (AdModelUtils.TT_Native_rate == 0) {
                showTTInstal(isTTVertical, true);
            }
        }
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail");
        if (!isVertical) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (AdModelUtils.TT_Native_rate == 0) {
                showTTInstal(isTTVertical, true);
            }
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

        if (mttFullVideoAd != null) {
            mttFullVideoAd = null;
        }
    }

    public interface OnLoadAdListener {
        void successed();

        void failed();

        void closed();
    }


}
