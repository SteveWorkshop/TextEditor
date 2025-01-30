package io.github.materialapps.texteditor.logic.network;

import com.google.ai.client.generativeai.type.GenerateContentResponse;

public interface AGIClient {

    void build();
    void rewriteContent(String text,String style,Bar1 success,Bar2 failure);
    void genMd(String text,Bar1 success,Bar2 failure);
    void summaryContent(String text,Bar1 success,Bar2 failure);
    void writeMeANote(String hint,Bar1 success,Bar2 failure);

    //成功回调
    interface Bar1{
        void foo(String text);
    }

    //失败回调
    interface Bar2{
        void foo(Throwable t);
    }
}
