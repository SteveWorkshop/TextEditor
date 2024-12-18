package io.github.materialapps.texteditor.logic.render;

import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;

public class BaseRender {
    protected void renderInlineSymbol(EditText editText, String symbol){
        int size=symbol.length();
        Editable editArea = editText.getEditableText();
        //这是原来的
        int sp = editText.getSelectionStart();
        int ep = editText.getSelectionEnd();
        if(sp<0||ep<0){return;}
        if (sp == ep) {
            editArea.insert(sp,symbol+symbol);
            editText.setSelection(sp+size);
        } else {
            editArea.insert(sp,symbol);
            editArea.insert(ep+size,symbol);//要偏移
            editText.setSelection(sp+size,ep+size);
        }
    }

    protected void renderStartSymbol(EditText editText,String symbol){
        if(TextUtils.isEmpty(symbol)){return;}
        Editable editArea = editText.getEditableText();
        //这是原来的
        int sp = editText.getSelectionStart();
        int osp=sp;
        if(sp<0){return;}
        if(editArea.length()==0){
            editArea.append(symbol);
            return;
        }
        //找换行符
        if(sp==editArea.length()){
            sp--;
        }
        for(;sp>=0&&editArea.charAt(sp)!='\n';sp--){
            ;
        }
        if(sp<0){
            //没有换行符
            editArea.insert(0,symbol);
            return;
        }
        else{
            //找到了换行符
            editArea.insert(sp+1,symbol);
        }
    }

    protected void renderBlock(EditText editText,String block){
        if(TextUtils.isEmpty(block)){return;}
        int sp = editText.getSelectionStart();
        int offset=block.length();
        int osp=sp;
        if(sp<0){return;}
        Editable editArea = editText.getEditableText();
        editArea.insert(sp,block);
        editText.setSelection(sp+offset);
    }
}
