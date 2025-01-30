package io.github.materialapps.texteditor.logic.network.impl;

import android.util.Log;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import java.util.Arrays;

import io.github.materialapps.texteditor.logic.network.AGIClient;
import lombok.Getter;
import lombok.Setter;

public class QianWenClient implements AGIClient {

    private static final String TAG = "QianWenClient";

    private String prebuiltHint1="请将下面的文字用";
    private String prebuiltHint2="风格改写润色，只输出改写后的内容，不要带提示信息：";

    @Getter
    @Setter
    private String apiKey;

    public QianWenClient(String apiKey){
        this.apiKey=apiKey;
    }

    @Override
    public void build() {
        //todo:我不知道该做什么
    }

    @Override
    public void rewriteContent(String text, String style, Bar1 success, Bar2 failure) {
        String input=prebuiltHint1+style+prebuiltHint2+text;
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(input)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                .model("qwen-plus")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        try {
            GenerationResult result = gen.call(param);
            String x = result.getOutput().getChoices().get(0).getMessage().getContent();
            success.foo(x);
        } catch (Exception e) {
            Log.e(TAG, "rewriteContent: ", e);
            failure.foo(e);
        }
    }

    @Override
    public void genMd(String text, Bar1 success, Bar2 failure) {

    }

    @Override
    public void summaryContent(String text, Bar1 success, Bar2 failure) {

    }

    @Override
    public void writeMeANote(String hint, Bar1 success, Bar2 failure) {

    }
}
