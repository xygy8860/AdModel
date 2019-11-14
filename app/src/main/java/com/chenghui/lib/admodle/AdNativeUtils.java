package com.chenghui.lib.admodle;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
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
        Random random = new Random();
        int rand = random.nextInt(100);
        //没有权限
        if (!AdModelUtils.isHavePermissions(activity) || rand < AdModelUtils.TT_Native_rate) {  //如果落在头条范围内，开屏头条 rand < 50
            refreshTTAd(0);
        } else {
            refreshAd(0);
        }
    }

    private void refreshAd(int count) {
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
            nativeExpressAD.loadAD(1);
        } catch (Exception e) {

        }
    }

    @Override
    public void onNoAD(AdError adError) {
        //Log.i(TAG, String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));

        if (count < 2) {
            refreshAd(count + 1);
        }
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
        loadListAd();
    }

    /**
     * 加载feed广告
     */
    private void loadListAd() {
        Random random = new Random();
        int rand = random.nextInt(100);

        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(rand < AdModelUtils.TT_video_rate ? AdModelUtils.TT_video_id : AdModelUtils.TT_Native_id)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(640, 320)
                .setAdCount(1) //请求广告数量为1到3条
                .build();
        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e("123", "error:" + message);
                refreshAd(0);
            }

            @Override
            public void onFeedAdLoad(List<TTFeedAd> ads) {

                Log.e("123", "onFeedAdLoad:");

                if (ads == null || ads.isEmpty()) {
                    return;
                }

                TTFeedAd ad = ads.get(0);
                ad.setActivityForDownloadApp(activity);

                View convertView = LayoutInflater.from(activity).inflate(R.layout.admodel_large_pic, layout, false);
                AdViewHolder adViewHolder = new AdViewHolder();
                adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
                adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
                adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
                adViewHolder.mLargeImage = (ImageView) convertView.findViewById(R.id.iv_listitem_image);
                adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
                adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
                adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
                adViewHolder.videoView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_video);

                layout.removeAllViews();
                layout.addView(convertView);

                bindData(convertView, adViewHolder, ad);
                if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_LARGE_IMG && ad.getImageList() != null && !ad.getImageList().isEmpty()) {
                    TTImage image = ad.getImageList().get(0);
                    if (image != null && image.isValid()) {
                        Glide.with(activity).load(image.getImageUrl()).into(adViewHolder.mLargeImage);
                    }
                } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO) {
                    //获取视频播放view,该view SDK内部渲染，在媒体平台可配置视频是否自动播放等设置。
                    View video = ad.getAdView();
                    if (video != null) {
                        if (video.getParent() == null) {
                            adViewHolder.videoView.removeAllViews();
                            adViewHolder.videoView.addView(video);
                        }
                    }
                }
            }
        });
    }

    private void bindData(View convertView, final AdViewHolder adViewHolder, TTFeedAd ad) {
        //设置dislike弹窗，这里展示自定义的dialog
        bindDislikeCustom(adViewHolder.mDislike, ad);

        //可以被点击的view, 也可以把convertView放进来意味item可被点击
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(convertView);
        //触发创意广告的view（点击下载或拨打电话）
        List<View> creativeViewList = new ArrayList<>();
        creativeViewList.add(adViewHolder.mCreativeButton);
        //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
        creativeViewList.add(convertView);
        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
        ad.registerViewForInteraction((ViewGroup) convertView, clickViewList, creativeViewList, new TTNativeAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View view, TTNativeAd ad) {
                /*if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被点击");
                }*/
            }

            @Override
            public void onAdCreativeClick(View view, TTNativeAd ad) {
                /*if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被创意按钮被点击");
                }*/
            }

            @Override
            public void onAdShow(TTNativeAd ad) {
                /*if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "展示");
                }*/
            }
        });
        adViewHolder.mTitle.setText(ad.getTitle()); //title为广告的简单信息提示
        adViewHolder.mDescription.setText(ad.getDescription()); //description为广告的较长的说明
        adViewHolder.mSource.setText(ad.getSource() == null ? "广告来源" : ad.getSource());
        TTImage icon = ad.getIcon();
        if (icon != null && icon.isValid()) {
            Glide.with(activity).load(icon.getImageUrl()).into(adViewHolder.mIcon);
        }
        Button adCreativeButton = adViewHolder.mCreativeButton;
        switch (ad.getInteractionType()) {
            case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                //如果初始化ttAdManager.createAdNative(getApplicationContext())没有传入activity 则需要在此传activity，否则影响使用Dislike逻辑
                if (activity instanceof Activity) {
                    ad.setActivityForDownloadApp((Activity) activity);
                }
                adCreativeButton.setVisibility(View.VISIBLE);
                /*if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.VISIBLE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.VISIBLE);*/
                //bindDownloadListener(adCreativeButton, adViewHolder, ad);
                //绑定下载状态控制器
                //bindDownLoadStatusController(adViewHolder, ad);
                break;
            case TTAdConstant.INTERACTION_TYPE_DIAL:
                adCreativeButton.setVisibility(View.VISIBLE);
                adCreativeButton.setText("立即拨打");
                /*if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.GONE);*/
                break;
            case TTAdConstant.INTERACTION_TYPE_LANDING_PAGE:
            case TTAdConstant.INTERACTION_TYPE_BROWSER:
//                    adCreativeButton.setVisibility(View.GONE);
                adCreativeButton.setVisibility(View.VISIBLE);
                adCreativeButton.setText("查看详情");
                /*if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.GONE);*/
                break;
            default:
                adCreativeButton.setVisibility(View.GONE);
                /*if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.GONE);*/
                //TToast.show(mContext, "交互类型异常");
        }
    }

    private void bindDislikeCustom(View dislike, final TTFeedAd ad) {
        /*List<FilterWord> words = ad.getFilterWords();
        if (words == null || words.isEmpty()) {
            return;
        }

        final DislikeDialog dislikeDialog = new DislikeDialog(mContext, words);
        dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
            @Override
            public void onItemClick(FilterWord filterWord) {
                //屏蔽广告
                mData.remove(ad);
                notifyDataSetChanged();
            }
        });
        final TTAdDislike ttAdDislike = ad.getDislikeDialog(dislikeDialog);

        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //展示dislike可以自行调用dialog
                dislikeDialog.show();

                //也可以使用接口来展示
                //ttAdDislike.showDislikeDialog();
            }
        });*/
    }

    public static class AdViewHolder {
        ImageView mIcon;
        ImageView mDislike;
        Button mCreativeButton;
        TextView mTitle;
        TextView mDescription;
        TextView mSource;
        ImageView mLargeImage;
        FrameLayout videoView;
    }

}
