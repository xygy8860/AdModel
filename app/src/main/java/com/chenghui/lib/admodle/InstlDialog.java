package com.chenghui.lib.admodle;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.Random;

/**
 * @author xygy
 * @version 2016-3-17 下午8:31:43
 *          类说明:插屏对话框
 */
public class InstlDialog {

    private Activity context;
    private AlertDialog dialog;
    public RelativeLayout layout;
    private ImageView close;
    private int mRand = 5; // 点击几率
    private AdInstalUtils.OnLoadAdListener listener;

    /*public InstlDialog(Activity context, boolean isShowClosedBtn) {
        this(context, isShowClosedBtn, 0);
    }

    public InstlDialog(Activity context, boolean isShowClosedBtn, int mRand) {
        this(context, isShowClosedBtn, mRand, null);
    }*/

    public InstlDialog(Activity context, boolean isShowClosedBtn, int mRand, AdInstalUtils.OnLoadAdListener listener) {
        this.context = context;
        this.mRand = mRand;
        this.listener = listener;

        dialog = new AlertDialog.Builder(context, R.style.admodel_dialog).create();
        dialog.show();
        dialog.getWindow().setContentView(R.layout.admodel_dialog_instal);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        layout = (RelativeLayout) dialog.findViewById(R.id.admodel_instl_layout);
        close = (ImageView) dialog.findViewById(R.id.admodel_instl_close);

        if (isShowClosedBtn) {
            close.setVisibility(View.VISIBLE);
            if (new Random().nextInt(100) > mRand) {
                setCloseListener();
            }
        }
    }

    public void setNativeAd(NativeExpressADView nativeExpressADView) {
        if (!layout.isShown()) {
            layout.setVisibility(View.VISIBLE);
        }

        if (layout.getChildCount() > 0) {
            layout.removeAllViews();
        }

        layout.addView(nativeExpressADView);
    }

    public void setNativeAd(View view) {
        if (!layout.isShown()) {
            layout.setVisibility(View.VISIBLE);
        }

        if (layout.getChildCount() > 0) {
            layout.removeAllViews();
        }

        layout.addView(view);
    }

    public void setCloseListener() {
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void dismissOnly() {
        dialog.dismiss();
    }

    public void dismiss() {
        if (layout != null && layout.getChildCount() > 0) {
            layout.removeAllViews();
            layout.setVisibility(View.GONE);
        }

        if (listener != null) {
            listener.failed();
        }

        dialog.dismiss();
    }

    public void show() {
        dialog.show();
    }

}
