package com.chenghui.lib.admodle;

import android.content.Context;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;

/**
 * Created by cdsunqinwei on 2018/3/21.
 */

public class AdModelUtils {
    // 百度广告初始化
    public static void initTTAd(Context context) {
        if (TextUtils.isEmpty(AdModelUtils.TT_Appid)) {
            return;
        }

        //强烈建议在应用对应的Application#onCreate()方法中调用，避免出现content为null的异常
        TTAdSdk.init(context,
                new TTAdConfig.Builder()
                        .appId(AdModelUtils.TT_Appid)
                        .useTextureView(false) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                        .appName(AdModelUtils.TT_Name)
                        //.titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                        .allowShowNotify(true) //是否允许sdk展示通知栏提示
                        .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                        //.debug(false) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_4G) //允许直接下载的网络状态集合
                        .supportMultiProcess(false) //是否支持多进程，true支持
                        .build());
    }


    // 横幅广告控制几率 默认百度100%
    public static int BD_Banner_rate = 0;
    public static int TT_Banner_rate = 0;
    public static int GDT_Banner_rate = 100;

    // 开屏广告控制几率 默认头条100%
    public static int BD_Splash_rate = 0;
    public static int TT_Splash_rate = 0;
    public static int GDT_Splash_rate = 100;

    public static String APPID = "";
    public static String SplashID = "";
    public static String NativeId_Img = ""; // 纯图片 竖图
    public static String NativeId_txt_img = ""; // 上文下图
    public static String NativeId_img_txt = ""; // 上图下文  1080037139439140
    public static String NativeId_leftImg_rightTxt = ""; // 左图右文
    public static String NativeId_Horizontal_Img = ""; // 纯图片 横图

    public static String BannerPosID = "";
    public static String InstalPosID = "";

    public static int mRand = 95; // 控制点击几率
    public static boolean isSplashFirst = true; // true:开屏优先  false:原生优先

    //    public static String SDK_KEY = "SDK20170014120225l4da2ffsad5b8vv";
    public static String SDK_KEY = "";


    // 以下百度广告配置
    public static String BD_Appid = "";
    public static String BD_Banner_id = "";
    public static String BD_Splash_id = "";

    // 以下头条广告配置
    public static String TT_Appid = "";
    public static String TT_Splash_id = ""; // 开屏广告id
    public static String TT_Banner_id = "";
    public static String TT_Name = "";

}
