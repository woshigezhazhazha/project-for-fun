package com.tizz.signin.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtils {
    private ProgressDialog pd;

    public void showProgressDialog(Context context,String title,String message){
        pd=new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle(title);
        pd.setMessage(message+"...");
        pd.setIndeterminate(false);
        pd.setCancelable(true);
        pd.show();
    }

    public void finishProgressDialog(){
        pd.cancel();
    }
}
