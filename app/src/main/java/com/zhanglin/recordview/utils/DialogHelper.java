package com.zhanglin.recordview.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogHelper {


    public static void showOneButtonDialog(Context mContext, String msgString,
                                           CharSequence btnString, boolean cancelable, final AlertDialog.OnClickListener positiveListener) {

        new AlertDialog.Builder(mContext)
                .setMessage(msgString)
                .setPositiveButton(btnString,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (positiveListener != null)
                                    positiveListener.onClick(dialog, which);
                                dialog.dismiss();

                            }
                        })
                .setCancelable(cancelable).show();
    }


    /**
     * 两个按钮的dialog
     *
     * @param context
     * @param message
     * @param okStr
     * @param cancelStr
     * @param cancelable
     * @param listener
     * @return
     */
    public static void showTwoButtonDialog(Context context, String message,
                                           String okStr, String cancelStr, final boolean cancelable, final AlertDialog.OnClickListener listener, final AlertDialog.OnClickListener cancelListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(okStr,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (listener != null)
                                    listener.onClick(dialog, which);
                                dialog.dismiss();

                            }
                        })
                .setNegativeButton(cancelStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cancelListener != null)
                            cancelListener.onClick(dialog, which);
                        dialog.dismiss();
                    }
                })
                .setCancelable(cancelable).show();
    }

}
