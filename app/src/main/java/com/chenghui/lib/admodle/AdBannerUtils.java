package com.chenghui.lib.admodle;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.BannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.comm.util.AdError;

import org.json.JSONObject;

import java.util.Random;

/**
 * Created by 孙勤伟 on 2017/2/6.
 */
public class AdBannerUtils {

    private static BannerView bv;

    // 百度广告初始化
    public static void initAd(Context context) {
        if (TextUtils.isEmpty(AdModelUtils.BD_Appid)) {
            return;
        }

        AdView.setAppSid(context, AdModelUtils.BD_Appid);
    }

    public static void initBanner(ViewGroup layout, ViewGroup gdt, Activity context) {

        Random random = new Random();

        int rand = random.nextInt(100);
        if (rand < AdModelUtils.BD_Banner_rate) { // 如果落在百度范围内则调用百度
            try {
                initAd(context);
                adviewBanner(layout, gdt, context);
            } catch (Throwable e) {

            }
        } else {
            gdtBanner(gdt, layout, context);
        }
    }

    // region 3.2.3 自定义插屏代码
    private static void adviewBanner(final ViewGroup adviewLayout, final ViewGroup mGdtLayout, final Activity context) {
        try {
            if (!isNetworkAvailable(context) || adviewLayout == null) {
                return;
            }


            View view = LayoutInflater.from(adviewLayout.getContext()).inflate(R.layout.admodel_bd_banner_layout, adviewLayout, false);
            ViewGroup bdLayout = view.findViewById(R.id.admodel_bd_banner_layout);
            ImageView close = view.findViewById(R.id.admodel_bd_banner_layout_close);

            //AppActivity.setActionBarColorTheme(AppActivity.ActionBarColorTheme.ACTION_BAR_WHITE_THEME);
            // 另外，也可设置动作栏中单个元素的颜色, 颜色参数为四段制，0xFF(透明度, 一般填FF)DE(红)DA(绿)DB(蓝)
            // AppActivity.getActionBarColorTheme().set[Background|Title|Progress|Close]Color(0xFFDEDADB);

            // 创建广告View
            final AdView adView = new AdView(adviewLayout.getContext(), AdModelUtils.BD_Banner_id);
            // 设置监听器
            adView.setListener(new AdViewListener() {
                public void onAdSwitch() {
                    Log.w("", "onAdSwitch");
                }

                public void onAdShow(JSONObject info) {
                    // 广告已经渲染出来
                    //Log.w("", "onAdShow " + info.toString());
                }

                public void onAdReady(AdView adView) {
                    // 资源已经缓存完毕，还没有渲染出来
                    Log.w("", "onAdReady " + adView);
                }

                public void onAdFailed(String reason) {
                    //Log.w("", "onAdFailed " + reason);
                }

                public void onAdClick(JSONObject info) {
                    // Log.w("", "onAdClick " + info.toString());

                }

                @Override
                public void onAdClose(JSONObject arg0) {
                    //Log.w("", "onAdClose");
                }
            });

            /*DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager) adviewLayout.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
            int winW = dm.widthPixels;
            int winH = dm.heightPixels;
            int width = Math.min(winW, winH);
            int height = width * 3 / 20;*/
            // 将adView添加到父控件中(注：该父控件不一定为您的根控件，只要该控件能通过addView能添加广告视图即可)
            /*RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(width, height);
            rllp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);*/

            adviewLayout.addView(view);
            bdLayout.addView(adView);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adView != null) {
                        adView.destroy();
                    }

                    if (adviewLayout != null) {
                        adviewLayout.removeAllViews();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
