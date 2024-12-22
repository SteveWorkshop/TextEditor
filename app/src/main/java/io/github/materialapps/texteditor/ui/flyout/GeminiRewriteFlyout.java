package io.github.materialapps.texteditor.ui.flyout;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.logic.network.AGIClient;
import lombok.Getter;
import lombok.Setter;

public class GeminiRewriteFlyout extends LinearLayout {

    private static final String TAG = "GeminiRewriteFlyout";

    private @Getter @Setter AGIClient client;
    private @Getter @Setter String buffer;
    private @Getter @Setter Activity activity;

    private EditText resultArea;
    private Button btnClose;
    private Button btnConfirm;
    private Button btnCancel;
    private Button btnRefresh;

    private @Getter @Setter Foo callback;

    private LinearLayout panelProcess;
    private LinearLayout panelError;

    //todo：可选
    public static final String DEFAULT_STYLE="书面语";

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

        btnRefresh.setOnClickListener(v->{
            rewrite();
        });

        btnConfirm.setOnClickListener(v->{
            callback.onConfirm(resultArea.getText().toString());
        });

        btnCancel.setOnClickListener(v->{
            callback.onCancel();
        });

        btnClose.setOnClickListener(v->{
            callback.onClose();
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
            panelProcess.setVisibility(VISIBLE);
            Log.d(TAG, "rewrite: ===========================工作==============================");
            client.rewriteContent(buffer,DEFAULT_STYLE,result->{
                if(!TextUtils.isEmpty(result)){
                    activity.runOnUiThread(()->{
                        resultArea.setText(result);
                        panelProcess.setVisibility(GONE);

                    });
                }
            },t->{
                Log.e(TAG, "rewrite: ", t);
                activity.runOnUiThread(()->{
                    panelProcess.setVisibility(GONE);
                    panelError.setVisibility(VISIBLE);
                });
            });
        }
    }

    public interface Foo{
        void onConfirm(String result);
        void onCancel();
        void onClose();
    }
}
