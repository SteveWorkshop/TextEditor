package io.github.materialapps.texteditor.logic.render;

import android.text.Editable;
import android.widget.EditText;

import io.github.materialapps.texteditor.util.FormatUtil;

public class FormatRender {
    public void renderBold(EditText editText){
        Editable editArea = editText.getEditableText();
        //这是原来的
        int sp = editText.getSelectionStart();
        int ep = editText.getSelectionEnd();
        if(sp<0||ep<0){return;}
        String bold = FormatUtil.getBold();
        if (sp == ep) {
            editArea.insert(sp,bold+bold);
            editText.setSelection(sp+2);
        } else {
            editArea.insert(sp,bold);
            editArea.insert(ep+2,bold);//要偏移
            editText.setSelection(sp+2,ep+2);
        }
    }

    public void renderItalic(EditText editText){
        Editable editArea = editText.getEditableText();
        //这是原来的
        int sp = editText.getSelectionStart();
        int ep = editText.getSelectionEnd();
        if(sp<0||ep<0){return;}
        String italic=FormatUtil.getItalic();
        if (sp == ep) {
            editArea.insert(sp,italic+italic);
            editText.setSelection(sp+1);
        }
        else {
            editArea.insert(sp,italic);
            editArea.insert(ep+1,italic);//要偏移
            editText.setSelection(sp+1,ep+1);
        }
    }
}
