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
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private String prebuiltHint2="风格改写润色，只输出改写后的内容，不要带提示信息：\n\n";

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
                //todo:硬编码版本号存在很严重的可用性问题！！！！必须修改！！
                "gemini-1.5-flash",
                apkKey,
                configBuilder.build(),
                safetySettings
        );
    }

    @Override
    public void rewriteContent(String text,String style,Bar1 success,Bar2 failure){
        String input=prebuiltHint1+style+prebuiltHint2+text;
        simpleTextGeneration(input,success,failure);
    }

    @Override
    public void genMd(String text, Bar1 success, Bar2 failure) {
        String input="请根据语义将下面这些文字整理为markdown： "+text+" ，只输出整理好的markdown结果";
        simpleTextGeneration(input,success,failure);
    }

    @Override
    public void summaryContent(String text, Bar1 success, Bar2 failure) {
        String input="请总结下面的文档： "+text+" ，只输出整理好的结果";
        simpleTextGeneration(input,success,failure);
    }

    @Override
    public void writeMeANote(String hint, Bar1 success, Bar2 failure) {
        String input="请帮我写一篇关于"+hint+"的文章，只输出写好的文章";
        simpleTextGeneration(input,success,failure);
    }

    private void simpleTextGeneration(String input,Bar1 success, Bar2 failure){
        if(gm==null){
            Log.e(TAG, "没有client数据");
            failure.foo(new RuntimeException("没有client数据"));
        }
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText(input)
                .build();
        Log.d(TAG, "simpleTextGeneration: &&&&&&&&&&&&&&&&&&&&&&&&&&&&&   "+input);
        Executor executor=new ThreadPoolExecutor(5,10,60, TimeUnit.SECONDS,new LinkedBlockingDeque<>());
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

    public void schedCall(String input,Bar1 success, Bar2 failure){
        String prompt="找到下面文章中出现的待办事项信息，输出一个JSON数组的格式（但不需要markdown的```块标记），每个数组元素是一个日程信息，属性包括，title：标题，content：详细内容，start_time：开始时间，end_time：结束时间，标题需要你自己总结，开始时间和结束时间请转换为UTC时间戳正整数，时区按照UTC+8计算，如果文章不包含待办事项信息，返回一个空JOSN数组。只需输出结果即可，不需要加markdown的代码块标记：\n"+input;
        simpleTextGeneration(prompt, new Bar1() {
            @Override
            public void foo(String text) {
                Log.d(TAG, "foo: "+text);
                success.foo(text);

            }
        }, new Bar2() {
            @Override
            public void foo(Throwable t) {
                failure.foo(t);
            }
        });
    }
}
