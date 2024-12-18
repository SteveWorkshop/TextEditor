package io.github.materialapps.texteditor.ui.flyout;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.logic.network.AGIClient;
import io.github.materialapps.texteditor.logic.render.DirectAIRender;
import lombok.Getter;
import lombok.Setter;

public class GeminiRewriteFlyout extends LinearLayout {

    private @Getter @Setter AGIClient client;
    private @Getter @Setter String buffer;
    private @Getter @Setter DirectAIRender render;

    private EditText resultArea;
    private Button btnClose;
    private Button btnConfirm;
    private Button btnCancel;
    private Button btnRefresh;

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
    }

    public void init(AGIClient client){
        this.client=client;
        render=new DirectAIRender(client);
    }

    private void rewrite(){
        if(client==null|| TextUtils.isEmpty(buffer)){
            Toast.makeText(getContext(), "请先选择内容", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            render.renderAI(resultArea,DEFAULT_STYLE,(Activity) getContext());
        }
    }

    public interface Foo{
        void onConfirm(String result);
        void onCancel(Throwable t);
        void onClose();
    }
}
