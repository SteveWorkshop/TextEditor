package io.github.materialapps.texteditor.logic.network;

import com.google.ai.client.generativeai.type.GenerateContentResponse;

public interface AGIClient {

    void build();
    void rewriteContent(String text,String style,Bar1 success,Bar2 failure);

    interface Bar1{
        void foo(String text);
    }

    interface Bar2{
        void foo(Throwable t);
    }
}
