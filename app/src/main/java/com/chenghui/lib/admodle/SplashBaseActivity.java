package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.Random;

/**
 * Created by cdsunqinwei on 2018/3/20.
 */

public abstract class SplashBaseActivity extends Activity {

    private static final String TAG = "123";
    protected boolean canJump = false;

    protected ViewGroup splashLayout; // 必须在子类赋值
    protected TextView mJumpBtn; // 必须在子类赋值

    //开屏广告加载超时时间,建议大于1000,这里为了冷启动第一次加载到广告并且展示,示例设置了2000ms
    private static final int AD_TIME_OUT = 2000;
    private static final int MSG_GO_MAIN = 1;

    //头条开屏强制跳转
    private boolean mForceJump;

    // 是否已经拉取过广告
    private int mGetAdFlag = 0; // 0:未获取广告 1：已获取一次广告  2：已获取两次广告

    private CountDownTimer downTimer;
    private SplashAD splashAD;

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
                loadSplashTTAd(0);
            } catch (Exception e) {
                QQKaiping(0);
            }
        } else {
            QQKaiping(0);
        }

        jishi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            openMain();
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
    private void loadSplashTTAd(final int count) {
        //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(AdModelUtils.TT_Splash_id)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .build();

        if (splashLayout == null) {
            next();
            return;
        }


        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(this);
        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            @MainThread
            public void onError(int code, String message) {
                if (count < 1) {
                    loadSplashTTAd(1);
                } else if (count < 2) {
                    QQKaiping(0);
                } else {
                    openMain();
                }
            }

            @Override
            @MainThread
            public void onTimeout() {
                if (count < 1) {
                    loadSplashTTAd(1);
                } else if (count < 2) {
                    QQKaiping(0);
                } else {
                    openMain();
                }
            }

            @Override
            @MainThread
            public void onSplashAdLoad(TTSplashAd ad) {
                mForceJump = true;

                //获取SplashView
                View view = ad.getSplashView();
                if (view == null || splashLayout == null) {
                    next();
                    return;
                }

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
                        if (downTimer != null) {
                            downTimer.cancel();
                        }
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        Log.d(TAG, "onAdShow");
                    }

                    @Override
                    public void onAdSkip() {
                        //Log.d(TAG, "onAdSkip");
                        next();
                    }

                    @Override
                    public void onAdTimeOver() {
                        //Log.d(TAG, "onAdTimeOver");
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
        if (mJumpBtn == null) {
            next();
            return;
        }

        mJumpBtn.setVisibility(View.VISIBLE);
        splashAD = new SplashAD(this, mJumpBtn,
                AdModelUtils.SplashID, new SplashADListener() {

            @Override
            public void onADDismissed() {
                next();
            }

            @Override
            public void onNoAD(AdError adError) {
                if (count < 1) {
                    QQKaiping(1);
                } else {
                    openMain();
                }
            }

            @Override
            public void onADPresent() {
                mJumpBtn.setBackgroundResource(R.drawable.admodel_bg_splash);
            }

            @Override
            public void onADClicked() {
                if (downTimer != null) {
                    downTimer.cancel();
                }
            }

            @Override
            public void onADTick(long l) {
                mJumpBtn.setText(" " + Math.round(l / 1000) + "跳过 ");
                //Log.e("123", "时长：" + l);
            }

            @Override
            public void onADExposure() {
                //Log.e("123", "onADExposure：");
                //jishi();
            }

            @Override
            public void onADLoaded(long l) {
                //Log.e("123", "onADLoaded：" + l);
                if (DownloadConfirmHelper.USE_CUSTOM_DIALOG) {
                    splashAD.setDownloadConfirmListener(DownloadConfirmHelper.DOWNLOAD_CONFIRM_LISTENER);
                }
            }
        }, 0);

        splashAD.fetchAndShowIn(splashLayout);
    }

    private void jishi() {
        downTimer = new CountDownTimer(8000, 8000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                openMain();
            }
        };
        downTimer.start();
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
                openMain();
            }
        }).start();
    }

    protected synchronized void next() {
        if (canJump) {
            openMain();
        } else {
            canJump = true;
        }
    }

    private void openMain() {
        try {
            if (downTimer != null) {
                downTimer.cancel();
            }
        } catch (Exception er) {

        }

        openMainActivity();
    }

    /**
     * 跳转到主页面
     */
    protected abstract void openMainActivity();


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
