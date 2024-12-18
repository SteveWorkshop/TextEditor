package io.github.materialapps.texteditor.logic.render;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import io.github.materialapps.texteditor.logic.network.AGIClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * used for directly render rewrite content
 */

@NoArgsConstructor
public class DirectAIRender extends BaseRender{

    private static final String TAG = "AIRender";

    @Getter
    @Setter
    private AGIClient client;

    public DirectAIRender(AGIClient client){this.client=client;}

    public void renderAI(EditText editText, String style, Activity activity){
        int sp = editText.getSelectionStart();
        int ep=editText.getSelectionEnd();
        if(sp<0||ep<0||sp<ep||client==null){
            //未选择文本
            return;
        }
        Editable editableText = editText.getEditableText();
        CharSequence charSequence = editableText.subSequence(sp, ep);
        renderAIbyString(String.valueOf(charSequence),style,activity);
    }

    public void renderAIbyString(String s, String style, Activity activity){
        if(TextUtils.isEmpty(s))
        client.rewriteContent(s,style, result->{
            activity.runOnUiThread(()->{
                if(!TextUtils.isEmpty(result)){

                }
            });
        },failure->{
            Log.e(TAG, "renderAI: ",failure);
            activity.runOnUiThread(()->{

            });
        });
    }
}
