package io.github.materialapps.texteditor.ui.flyout;

import android.app.Activity;
import android.content.Context;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;

import org.commonmark.node.Node;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.logic.entity.dto.SchedCallDTO;
import io.github.materialapps.texteditor.logic.network.AGIClient;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import lombok.Getter;
import lombok.Setter;

public class GeminiRewriteFlyout extends LinearLayout {

    private static final String TAG = "GeminiRewriteFlyout";

    private @Getter @Setter AGIClient client;
    private @Getter @Setter String buffer;
    private @Getter @Setter Activity activity;

    private @Getter @Setter boolean needToInject=false;

    private Markwon markwon;

    private EditText resultArea;
    private Button btnClose;
    private Button btnConfirm;
    private Button btnCancel;
    private Button btnRefresh;

    private @Getter @Setter Foo callback;

    private LinearLayout panelProcess;
    private LinearLayout panelError;

    //todo：可选
    public static final String DEFAULT_REWRITE_STYLE ="书面语";
    public static final String[] LOADING_HINTS=new String[]{
            "东市买骏马，西市买鞍鞯，南市买辔头，北市买长鞭。\n正在为您获取结果",
            "运筹帷幄之中，决胜千里之外。\n请稍后",
            "海日生残夜，江春入旧年。\n你的智能助手即将就绪。",
            "海阔凭鱼跃，天高任鸟飞。潮平两岸阔，风正一帆悬。\n精彩旅程即将开启。",
            "逝者如斯夫,不舍昼夜。\n请稍候,我们即将完成。",
            "春风得意马蹄疾，一日看尽长安花。\n请稍候,我们即将完成。"
    };

    private int currentIndex=0;

    public GeminiRewriteFlyout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.flyout_copilot_rewrite,this);
        resultArea=findViewById(R.id.txb_rewrite_preview);
        btnClose=findViewById(R.id.btn_copilot_close);
        btnConfirm=findViewById(R.id.btn_rewrite_accept);
        btnCancel=findViewById(R.id.btn_rewrite_cancel);
        btnRefresh=findViewById(R.id.btn_rewrite_refresh);
        panelProcess=findViewById(R.id.panel_process);
        panelError=findViewById(R.id.panel_error);

        markwon=Markwon.builder(resultArea.getContext())
                // create default instance of TablePlugin
                .usePlugin(TablePlugin.create(resultArea.getContext()))
                .usePlugin(GlideImagesPlugin.create(Glide.with(getContext())))
                .build();

        btnRefresh.setOnClickListener(v->{
            //rewrite();
        });

        btnConfirm.setOnClickListener(v->{
            if(callback!=null){callback.onConfirm(resultArea.getText().toString());}
            buffer=null;
        });

        btnCancel.setOnClickListener(v->{
            if(callback!=null){ callback.onCancel();}
            buffer=null;
        });

        btnClose.setOnClickListener(v->{
            if(callback!=null){callback.onClose();}
            buffer=null;
        });
    }

    public void init(AGIClient client){
        this.client=client;
    }

    public void rewrite(){

        if(client==null|| TextUtils.isEmpty(buffer)){
            Toast.makeText(getContext(), "请先选择内容", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            prepareUI();
            client.rewriteContent(buffer, DEFAULT_REWRITE_STYLE, result->{
                handleSuccess(result,true,1);
            },t->{
                handleError(t);
            });
        }
    }

    public void summarize(String text){
        if(TextUtils.isEmpty(text)){return;}
        else{
            prepareUI();
            client.summaryContent(text,result->{
                handleSuccess(result,false,0);
            },t->{
                handleError(t);
            });
        }
    }

    public void genMd(String text){
        if(TextUtils.isEmpty(text)){return;}
        else{
            prepareUI();
            client.genMd(text,result->{
                handleSuccess(result,true,1);
            },t->{
                handleError(t);
            });
        }
    }

    public void aiGen(String text){
        if(TextUtils.isEmpty(text)){return;}
        else{
            prepareUI();
            client.writeMeANote(text,result->{
                handleSuccess(result,true,1);
            },t->{
                handleError(t);
            });
        }
    }

    public void test(String text){
        client.schedCall(text,result->{
            Log.d(TAG, "test: "+result);
            

        },t->{

        });
    }



    private void handleSuccess(String result,boolean needToInject,int mode){
        Node node = markwon.parse(result);
        Spanned markdown = markwon.render(node);
        activity.runOnUiThread(()->{
            markwon.setParsedMarkdown(resultArea, markdown);
            switch (mode){
                case 0:{
                    break;
                }
                case 1:{
                    this.needToInject=true;
                }
                case 2:{

                }
                default:{break;}
            }
            //this.needToInject=needToInject;
            postSuccessUI();
        });
    }

    private void handleError(Throwable t){
        Log.e(TAG, "", t);
        activity.runOnUiThread(()->{
            postErrorUI();
        });
    }

    private void prepareUI(){
//        resultArea.setText(LOADING_HINTS[currentIndex]);
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                int num=0;
//                while (num==currentIndex){
//                    num = (int)(Math.random() * LOADING_HINTS.length);
//                }
//                int finalNum = num;
//                activity.runOnUiThread(()->{
//                    resultArea.setText(LOADING_HINTS[finalNum]);
//                });
//            }
//        }, 0, 5000);

        panelProcess.setVisibility(VISIBLE);
    }

    private void postSuccessUI(){
        panelProcess.setVisibility(GONE);
    }

    private void postErrorUI(){
        panelProcess.setVisibility(GONE);
        panelError.setVisibility(VISIBLE);
    }

    public interface Foo{
        void onConfirm(String result);
        void onCancel();
        void onClose();
    }
}
