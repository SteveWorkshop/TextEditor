package io.github.materialapps.texteditor.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.github.materialapps.texteditor.R;

public class TroubleShootUtil {
    public static void bettaFishHint(Context context){
        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(context);
        builder.setTitle("正在开发的功能");
        View dialogView= LayoutInflater.from(context).inflate(R.layout.beta_dialog,null);
        builder.setView(dialogView);
        builder.setPositiveButton("我知道了",(dialog, which) -> {});
        builder.show();
    }
}
