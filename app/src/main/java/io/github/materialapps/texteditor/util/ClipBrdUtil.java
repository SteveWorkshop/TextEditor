package io.github.materialapps.texteditor.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class ClipBrdUtil {
    private Context context;

    public ClipBrdUtil(Context context) {
        this.context = context;
    }

    public void copyToClipboard(String text) {
        // 1. 获取ClipboardManager实例
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // 2. 创建ClipData对象
        ClipData clip = ClipData.newPlainText("simple text", text);

        // 3. 设置剪贴板内容
        clipboard.setPrimaryClip(clip);

        // 提示用户已复制
        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    public String paste(){
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
            CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
            String addedTextString = String.valueOf(addedText);
            if (!TextUtils.isEmpty(addedTextString)) {
                return addedTextString;
            }
        }
        return "";
    }
}
