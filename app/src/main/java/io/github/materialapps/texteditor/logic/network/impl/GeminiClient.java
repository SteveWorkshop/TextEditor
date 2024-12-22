package io.github.materialapps.texteditor.logic.network.impl;

import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import io.github.materialapps.texteditor.logic.network.AGIClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class GeminiClient implements AGIClient {

    private static final String TAG = "GeminiClient";

    @Getter
    @Setter
    private String apkKey;

    //todo:用户自定义
    private String prebuiltHint1="请将下面的文字用";
    private String prebuiltHint2="风格改写润色，只输出改写后的内容，不要带提示信息：";

    private GenerativeModel gm;
    public GeminiClient(String apkKey){
        this.apkKey=apkKey;
    }

    public void build(){
        if(apkKey==null){
            return;
        }
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.15f;
        configBuilder.topK = 32;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 4096;

        List<SafetySetting> safetySettings = new ArrayList();
        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE));

        gm = new GenerativeModel(
                "gemini-1.5-flash-001",//todo：切换版本
                apkKey,
                configBuilder.build(),
                safetySettings
        );
    }

    @Override
    public void rewriteContent(String text,String style,Bar1 success,Bar2 failure){
        if(gm==null){
            Log.e(TAG, "没有client数据");
            return;
        }
        String input=prebuiltHint1+style+prebuiltHint2+text;
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText(input)
                .build();
        Executor executor= Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                success.foo(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                failure.foo(t);
            }
        }, executor);
    }
}
