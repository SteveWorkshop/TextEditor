package io.github.materialapps.texteditor.logic.render;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;

import io.github.materialapps.texteditor.util.FormatUtil;
import io.github.materialapps.texteditor.util.IDUtil;

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

    public void renderTable(EditText editText,int row,int col){
        String table=FormatUtil.getTableFrame(row,col);
        if(!TextUtils.isEmpty(table)){
            renderBlock(editText,table);
        }
    }

    public void renderImg(EditText editText, Bitmap bitmap, Activity activity){
        int sp = editText.getSelectionStart();
        if(sp<0){return;}
        //第一部分

        StringBuilder sb=new StringBuilder();
        sb.append("\n");
        sb.append(FormatUtil.IMG_TAG_HEAD_FORMAT_CONTROLLER);
        sb.append(FormatUtil.TAG_DEFAULT);
        sb.append(FormatUtil.IMG_TAG_CLOSE_FORMAT_CONTROLLER);
        String uuid= IDUtil.getUUID();
        sb.append("[");
        sb.append(uuid);
        sb.append(FormatUtil.IMG_TAG_CLOSE_FORMAT_CONTROLLER);
        sb.append("\n");
        //Log.d(TAG, "renderImg: 表头："+sb);
        renderBlock(editText,sb.toString());
        //第二部分（异步实现）

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        final ListenableFuture<String> listenableFuture = executorService.submit(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean compress = bitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);
            if(compress){
                byte[] byteArray = baos.toByteArray();
                String body = Base64.encodeToString(byteArray, Base64.DEFAULT);
                body = body.replaceAll("\r|\n", "");
                String div=FormatUtil.WARNING_DIVIDER;
                String header="data:image/png;base64,";
                StringBuilder bb=new StringBuilder();
                bb.append("\n");
                bb.append(div);
                bb.append("\n");
                bb.append(FormatUtil.IMG_DATA_INLINE_HEAD_FORMAT_CONTROLLER);
                bb.append(uuid);
                bb.append(FormatUtil.IMG_DATA_INLINE_CLOSE_FORMAT_CONTROLLER);
                bb.append(":");
                bb.append(header);
                bb.append(body);

                System.out.println("==========="+bb);

                return bb.toString();
            }
            else{
                return "";
            }

        });
        Futures.addCallback(listenableFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(String result) {
                //System.out.println("get listenable future's result with callback " + result);
                activity.runOnUiThread(()->{
                    editText.getEditableText().append(result);
                    editText.setSelection(sp);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "onFailure: ", t);;
            }
        },executorService);
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
