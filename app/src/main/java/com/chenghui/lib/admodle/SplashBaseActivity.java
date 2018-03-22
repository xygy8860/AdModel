package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.List;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initAdParams();

        if (AdModelUtils.isSplashFirst) {
            QQKaiping(0);
        } else {
            refreshAd(0);
        }
    }

    /**
     * 初始化参数
     */
    protected abstract void initAdParams();


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
        canJump = false;
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
            handler = null;
            timeRunnable = null;
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
                Log.e("123", "adError : " + adError.getErrorMsg());
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
        }, 0);
    }

    private void splash() {
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

    private void next() {
        if (canJump) {
            openMainActivity();
        } else {
            canJump = true;
        }
    }


    private void refreshAd(final int count) {
        try {
            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            nativeExpressAD = new NativeExpressAD(this, new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT), AdModelUtils.APPID,
                    AdModelUtils.NativeId_Img, new NativeExpressAD.NativeExpressADListener() {
                @Override
                public void onNoAD(AdError adError) {
                    Log.i(
                            TAG,
                            String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(),
                                    adError.getErrorMsg()));

                    if (count < 3) {
                        refreshAd(count + 1);
                    } else {
                        if (!AdModelUtils.isSplashFirst) {
                            QQKaiping(0);
                        } else {
                            next();
                        }
                    }
                }

                @Override
                public void onADLoaded(List<NativeExpressADView> adList) {
                    Log.i(TAG, "onADLoaded: " + adList.size());
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

                    try {
                        nativeExpressADView = adList.get(0);
                        // 广告可见才会产生曝光，否则将无法产生收益。
                        splashLayout.addView(nativeExpressADView);
                        nativeExpressADView.render();
                    } catch (Exception e) {
                        next();
                    }
                }

                @Override
                public void onRenderFail(NativeExpressADView adView) {
                    Log.i(TAG, "onRenderFail");
                    if (!AdModelUtils.isSplashFirst) {
                        QQKaiping(0);
                    } else {
                        splash();
                    }
                }

                @Override
                public void onRenderSuccess(NativeExpressADView adView) {
                    Log.i(TAG, "onRenderSuccess");
                    if (handler == null) {
                        handler = new Handler();
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
                    Log.i(TAG, "onADClosed");
                    // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，释放资源，不可以再用来展示。
                    if (splashLayout != null && splashLayout.getChildCount() > 0) {
                        splashLayout.removeAllViews();
                        splashLayout.setVisibility(View.GONE);
                    }
                    //next();
                }

                @Override
                public void onADLeftApplication(NativeExpressADView adView) {
                    Log.i(TAG, "onADLeftApplication");
                }

                @Override
                public void onADOpenOverlay(NativeExpressADView adView) {
                    Log.i(TAG, "onADOpenOverlay");
                }

                @Override
                public void onADCloseOverlay(NativeExpressADView adView) {
                    Log.i(TAG, "onADCloseOverlay");
                }

            }); // 这里的Context必须为Activity
            nativeExpressAD.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // 设置什么网络环境下可以自动播放视频
                    .setAutoPlayMuted(true) // 设置自动播放视频时，是否静音
                    .build()); // setVideoOption是可选的，开发者可根据需要选择是否配置
            nativeExpressAD.loadAD(1);
        } catch (NumberFormatException e) {
            Log.e("123", "ad size invalid.");
            Toast.makeText(this, "请输入合法的宽高数值", Toast.LENGTH_SHORT).show();
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
        private long t = 5000;

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
}
