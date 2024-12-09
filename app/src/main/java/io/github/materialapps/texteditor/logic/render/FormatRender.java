package io.github.materialapps.texteditor.logic.render;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import io.github.materialapps.texteditor.util.FormatUtil;

public class FormatRender {

    private static final String TAG = "FormatRender";

    public void renderBold(EditText editText){
        String bold = FormatUtil.getBold();
        renderInlineSymbol(editText,bold);
    }

    public void renderItalic(EditText editText){
        String italic=FormatUtil.getItalic();
        renderInlineSymbol(editText,italic);
    }

    public void renderHeader(EditText editText,int level){
        if(level<=0){return; }
        String header=FormatUtil.getHeader(level);
        renderStartSymbol(editText,header);
    }

    public void renderUl(EditText editText){
        String ul=FormatUtil.getUl();
        renderStartSymbol(editText,ul);
    }

    public void renderOl(EditText editText,int num){
        String ol=FormatUtil.getOl(num);
        renderStartSymbol(editText,ol);
    }

    public void renderLine(EditText editText){
        String line=FormatUtil.getLine();
        renderBlock(editText,line);
    }

    private void renderInlineSymbol(EditText editText,String symbol){
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

    private void renderStartSymbol(EditText editText,String symbol){
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

    private void renderBlock(EditText editText,String block){
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
