package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.List;
import java.util.Random;

/**
 * Created by cdsunqinwei on 2018/3/20.
 */

public abstract class SplashBaseActivity extends Activity {

    private static final String TAG = "123";
    protected boolean canJump = false;

    protected ViewGroup splashLayout; // 必须在子类赋值
    protected TextView mJumpBtn; // 必须在子类赋值

    // 测试id
//    protected String appId = "1106414865";
//    protected String splashID = "4050220679022649";
//    protected String nativeId = "5080737128844271";
//    protected int mRand = 95; // 控制点击几率
//    protected boolean isSplashFirst = true; // true:开屏优先  false:原生优先

    private NativeExpressADView nativeExpressADView;
    private NativeExpressAD nativeExpressAD;

    private Handler handler;
    private TimeRunnable timeRunnable;
    private TimeOutRunnable timeOutRunnable;

    //开屏广告加载超时时间,建议大于1000,这里为了冷启动第一次加载到广告并且展示,示例设置了2000ms
    private static final int AD_TIME_OUT = 2000;
    private static final int MSG_GO_MAIN = 1;
    //开屏广告是否已经加载
    private boolean mHasLoaded;

    //头条开屏强制跳转
    private boolean mForceJump;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * 初始化参数
     */
    protected void initAdParams() {
        Random random = new Random();
        int rand = random.nextInt(100);

        canJump = false;

        /**
         * 例如：TT=50 BD=30
         *
         */
        if (rand < AdModelUtils.TT_Splash_rate) {  //如果落在头条范围内，开屏头条 rand < 50
            try {
                loadSplashTTAd();
            } catch (Exception e) {
                QQKaiping(0);
            }
        } else {
            if (AdModelUtils.isSplashFirst) {
                QQKaiping(0);
            } else {
                refreshAd(0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            openMainActivity();
        }
        canJump = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mForceJump) { // 如果是头条开屏，那么因为倒计时暂停，那么返回后必须强制跳转主页
            canJump = true;
        } else {
            canJump = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (splashLayout != null) {
            splashLayout.removeAllViews();
            splashLayout = null;
        }

        // 使用完了每一个NativeExpressADView之后都要释放掉资源
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }

        if (handler != null) {
            if (timeRunnable != null) {
                handler.removeCallbacks(timeRunnable);
            }
            if (timeOutRunnable != null) {
                handler.removeCallbacks(timeOutRunnable);
            }
            handler = null;
            timeRunnable = null;
        }

        canJump = false;
    }

    /**
     * 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 加载开屏广告
     */
    private void loadSplashTTAd() {
        //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(AdModelUtils.TT_Splash_id)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .build();

        splashLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mHasLoaded) {
                    QQKaiping(0);
                }
            }
        }, AD_TIME_OUT);

        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(this);
        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            @MainThread
            public void onError(int code, String message) {
                mHasLoaded = true;
                QQKaiping(0);
            }

            @Override
            @MainThread
            public void onTimeout() {
                mHasLoaded = true;
                QQKaiping(0);
            }

            @Override
            @MainThread
            public void onSplashAdLoad(TTSplashAd ad) {
                mHasLoaded = true;
                mForceJump = true;

                //获取SplashView
                View view = ad.getSplashView();
                splashLayout.removeAllViews();
                //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
                splashLayout.addView(view);
                //设置不开启开屏广告倒计时功能以及不显示跳过按钮,如果这么设置，您需要自定义倒计时逻辑
                //ad.setNotAllowSdkCountdown();

                //设置SplashView的交互监听器
                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
                        Log.d(TAG, "onAdClicked");
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        Log.d(TAG, "onAdShow");
                    }

                    @Override
                    public void onAdSkip() {
                        Log.d(TAG, "onAdSkip");
                        next();
                    }

                    @Override
                    public void onAdTimeOver() {
                        Log.d(TAG, "onAdTimeOver");
                        next();
                    }
                });
            }
        }, AD_TIME_OUT);
    }

    /**
     * 开屏
     *
     * @param count
     */
    private void QQKaiping(final int count) {
        mJumpBtn.setVisibility(View.VISIBLE);
        SplashAD splashAD = new SplashAD(this, splashLayout, mJumpBtn,
                AdModelUtils.APPID, AdModelUtils.SplashID, new SplashADListener() {
            @Override
            public void onADDismissed() {
                next();
            }

            @Override
            public void onNoAD(AdError adError) {
                String err = adError.getErrorMsg();
                if (!TextUtils.isEmpty(err) && err.contains("网络类型错误")) {
                    if (AdModelUtils.isSplashFirst) { // 如果是开屏优先，则无数据请求原生
                        refreshAd(0);
                    } else { // 如果是原生优先，开屏无数据则跳转主页
                        next();
                    }
                } else if (count < 2) {
                    QQKaiping(count + 1);
                } else {
                    if (AdModelUtils.isSplashFirst) { // 如果是开屏优先，则无数据请求原生
                        refreshAd(0);
                    } else { // 如果是原生优先，开屏无数据则跳转主页
                        next();
                    }
                }
            }

            @Override
            public void onADPresent() {
                mJumpBtn.setBackgroundResource(R.drawable.admodel_bg_splash);
            }

            @Override
            public void onADClicked() {

            }

            @Override
            public void onADTick(long l) {
                mJumpBtn.setText(" " + Math.round(l / 1000) + "跳过 ");
            }

            @Override
            public void onADExposure() {

            }
        }, 0);
    }

    protected void splash() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                openMainActivity();
            }
        }).start();
    }

    protected void next() {
        if (canJump) {
            openMainActivity();
        } else {
            canJump = true;
        }
    }


    private void refreshAd(final int count) {

        if (count == 0) {
            if (handler == null) {
                handler = new Handler();
            }

            if (timeOutRunnable == null) {
                timeOutRunnable = new TimeOutRunnable();
            }

            handler.postDelayed(timeOutRunnable, 4000);
        }

        try {
            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            nativeExpressAD = new NativeExpressAD(this, new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT), AdModelUtils.APPID,
                    AdModelUtils.NativeId_Img, new NativeExpressAD.NativeExpressADListener() {
                @Override
                public void onNoAD(AdError adError) {
                    try {
                        if (count < 3) {
                            refreshAd(count + 1);
                        } else {
                            if (!AdModelUtils.isSplashFirst) {
                                QQKaiping(0);
                            } else {
                                next();
                            }
                        }
                    } catch (Exception e) {

                    }
                }

                @Override
                public void onADLoaded(List<NativeExpressADView> adList) {
                    try {
                        // 释放前一个展示的NativeExpressADView的资源
                        if (nativeExpressADView != null) {
                            nativeExpressADView.destroy();
                        }

                        if (splashLayout.getVisibility() != View.VISIBLE) {
                            splashLayout.setVisibility(View.VISIBLE);
                        }

                        if (splashLayout.getChildCount() > 0) {
                            splashLayout.removeAllViews();
                        }

                        nativeExpressADView = adList.get(0);
                        // 广告可见才会产生曝光，否则将无法产生收益。
                        splashLayout.addView(nativeExpressADView);
                        nativeExpressADView.render();
                    } catch (Exception e) {
                        //next();
                    }
                }

                @Override
                public void onRenderFail(NativeExpressADView adView) {
                    if (!AdModelUtils.isSplashFirst) {
                        QQKaiping(0);
                    } else {
                        splash();
                    }
                }

                @Override
                public void onRenderSuccess(NativeExpressADView adView) {
                    if (handler == null) {
                        handler = new Handler();
                    }

                    if (timeOutRunnable != null) {
                        handler.removeCallbacks(timeOutRunnable);
                    }

                    timeRunnable = new TimeRunnable();
                    handler.postDelayed(timeRunnable, 10);
                    mJumpBtn.setVisibility(View.VISIBLE);

                    int random = (int) (Math.random() * 100);
                    if (random < AdModelUtils.mRand) {
                        mJumpBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                next();
                            }
                        });
                    }
                }

                @Override
                public void onADExposure(NativeExpressADView adView) {
                    Log.i(TAG, "onADExposure");
                }

                @Override
                public void onADClicked(NativeExpressADView adView) {
                    Log.i(TAG, "onADClicked");
                }

                @Override
                public void onADClosed(NativeExpressADView adView) {
                    // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，释放资源，不可以再用来展示。
                    if (splashLayout != null && splashLayout.getChildCount() > 0) {
                        splashLayout.removeAllViews();
                        splashLayout.setVisibility(View.GONE);
                    }
                    //next();
                }

                @Override
                public void onADLeftApplication(NativeExpressADView adView) {
                }

                @Override
                public void onADOpenOverlay(NativeExpressADView adView) {
                }

                @Override
                public void onADCloseOverlay(NativeExpressADView adView) {
                }

            }); // 这里的Context必须为Activity
            nativeExpressAD.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // 设置什么网络环境下可以自动播放视频
                    .setAutoPlayMuted(true) // 设置自动播放视频时，是否静音
                    .build()); // setVideoOption是可选的，开发者可根据需要选择是否配置
            nativeExpressAD.loadAD(1);
        } catch (Exception e) {
            next();
        }
    }

    /**
     * 跳转到主页面
     */
    protected abstract void openMainActivity();


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class TimeRunnable implements Runnable {
        public long t = 5000;

        @Override
        public void run() {
            if (t <= 0) {
                next();
            } else {
                mJumpBtn.setText(" " + Math.round(t / 1000) + "跳过 ");
                t = t - 100;
                if (t > 100) {
                    handler.postDelayed(timeRunnable, 100);
                } else {
                    handler.postDelayed(timeRunnable, 10);
                }
            }
        }
    }

    class TimeOutRunnable implements Runnable {

        @Override
        public void run() {
            next();
        }
    }
}
