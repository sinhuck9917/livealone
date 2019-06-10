package com.example.livealone4.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

public class ProgressDialogHelper {

    private static ProgressDialog progressDialog;
    private static ProgressBar progressBar;

    public static void show(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("잠시만 기다려주세요");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    //원하는 메시지로
    public static void show(Context context, String message){
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void setMessage(String message){
        if(progressDialog == null)
            return;

        progressDialog.setMessage(message);

    }

    public static void dismiss(){
        if(progressDialog !=null)
            progressDialog.dismiss();
    }

    public static void dismissProgressBarInMainActivity(){


    }
}
