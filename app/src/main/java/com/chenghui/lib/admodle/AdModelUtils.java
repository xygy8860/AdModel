package com.chenghui.lib.admodle;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;
import com.bytedance.sdk.openadsdk.TTLocation;
import com.qq.e.comm.managers.GDTADManager;

/**
 * Created by cdsunqinwei on 2018/3/21.
 */

public class AdModelUtils {

    /**
     * 需要进行检测的权限数组
     */
    protected static String[] needPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    public static void initGDT(Context context) {
        GDTADManager.getInstance().initWith(context, AdModelUtils.APPID);
    }

    // 头条广告初始化
    public static void initTTAdHuawei(Context context) {
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
                        //.allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                        //.debug(false) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .directDownloadNetworkType() //弹窗确认
                        .supportMultiProcess(false) //是否支持多进程，true支持
                        .customController(new TTCustomController() {
                            @Override
                            public boolean isCanUseLocation() {
                                return false;
                            }

                            @Override
                            public TTLocation getTTLocation() {
                                return null;
                            }

                            @Override
                            public boolean alist() {
                                return false;
                            }

                            @Override
                            public boolean isCanUsePhoneState() {
                                return false;
                            }

                            @Override
                            public String getDevImei() {
                                return null;
                            }

                            @Override
                            public boolean isCanUseWifiState() {
                                return false;
                            }

                            @Override
                            public boolean isCanUseWriteExternal() {
                                return super.isCanUseWriteExternal();
                            }

                            @Override
                            public String getDevOaid() {
                                return TT_deviceid;
                            }
                        })
                        .build());

        DownloadConfirmHelper.USE_CUSTOM_DIALOG = true;
    }

    // 头条广告初始化
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
                        //.allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                        //.debug(false) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        //.directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_4G) //允许直接下载的网络状态集合
                        .supportMultiProcess(false) //是否支持多进程，true支持
                        .customController(new TTCustomController() {
                            @Override
                            public boolean isCanUseLocation() {
                                return false;
                            }

                            @Override
                            public TTLocation getTTLocation() {
                                return null;
                            }

                            @Override
                            public boolean alist() {
                                return false;
                            }

                            @Override
                            public boolean isCanUsePhoneState() {
                                return false;
                            }

                            @Override
                            public String getDevImei() {
                                return null;
                            }

                            @Override
                            public boolean isCanUseWifiState() {
                                return false;
                            }

                            @Override
                            public boolean isCanUseWriteExternal() {
                                return super.isCanUseWriteExternal();
                            }

                            @Override
                            public String getDevOaid() {
                                return TT_deviceid;
                            }
                        })
                        .build());
    }

    // 判断是否有权限，没有权限直接TT
    public static boolean isHavePermissions(Activity activity) {
        try {
            for (String perm : needPermissions) {
                // 如果用没有授权
                if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }

            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }


    // 横幅广告控制几率
    public static int TT_Banner_rate = 0;
    public static int GDT_Banner_rate = 100;

    // 开屏广告控制几率 默认头条100%
    public static int TT_Splash_rate = 0;
    public static int GDT_Splash_rate = 100;

    // 原生广告头条比例 默认为0  序列1 分配广点通和头条
    public static int TT_Native_rate = 0;
    public static int TT_Native_model_rate = 100; // 序列2：头条原生中个性化模板混合的比例  其余为视频

    public static String APPID = "";
    public static String SplashID = "";
    public static String NativeId_Img = ""; // 纯图片 竖图
    public static String NativeId_txt_img = ""; // 上文下图
    public static String NativeId_img_txt = ""; // 上图下文  1080037139439140
    public static String NativeId_leftImg_rightTxt = ""; // 左图右文
    public static String NativeId_Horizontal_Img = ""; // 纯图片 横图
    public static String BannerPosID_2 = ""; // 2.0banner
    public static String InstalPosID = "";

    public static int mRand = 95; // 控制点击几率

    // 以下头条广告配置
    public static String TT_Appid = "";
    public static String TT_Splash_id = ""; // 开屏广告id
    public static String TT_Banner_id = "";
    public static String TT_Name = "";
    public static String TT_native_video_id = ""; // 原生 个性化模板 视频
    public static String TT_native_id = ""; // 原生 个性化模板 混合
    public static String TT_instal_id = ""; // 头条 插屏
    public static String TT_instal_Horizontal_id = ""; // 头条 插屏 横屏

    public static String TT_deviceid = ""; // 用户id

}
