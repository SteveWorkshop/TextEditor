package io.github.materialapps.texteditor.ui.fragment;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.github.materialapps.texteditor.BaseApplication;
import lombok.Getter;
import lombok.Setter;

public class EditorViewModel extends ViewModel {

    public static final int ZOOM_INC=2;//todo:自定义步进
    public static final int ZOOM_DEFAULT=18;

    @Getter
    @Setter
    private MutableLiveData<Boolean> show2Panel=new MutableLiveData<>(true);

    @Getter
    @Setter
    private MutableLiveData<Boolean> markdownMode=new MutableLiveData<>(true);

    //todo:持久化设置
    @Getter
    @Setter
    private MutableLiveData<Integer> uiSize=new MutableLiveData<>(18);


    //==============================================================================

    @Getter
    @Setter
    private MutableLiveData<String> currentText=new MutableLiveData<>();

    @Getter
    @Setter
    private MutableLiveData<Boolean> instanceType=new MutableLiveData<>(BaseApplication.NEW_FILE);

    @Getter
    @Setter
    private MutableLiveData<Boolean> hasEdited=new MutableLiveData<>(false);

    @Getter
    @Setter
    private LiveData<Boolean> needToSave;

    @Getter
    @Setter
    private MutableLiveData<Uri> currentFileUri=new MutableLiveData<>();

    public void incSize(){
        Integer cur = uiSize.getValue();
        if(cur!=null && cur+ZOOM_INC<=BaseApplication.MAX_UI_SIZE){
            uiSize.setValue(cur+ZOOM_INC);
        }

    }

    public void decSize(){
        Integer cur = uiSize.getValue();
        if(cur!=null && cur-ZOOM_INC>=BaseApplication.MIN_UI_SIZE){
            uiSize.setValue(cur-ZOOM_INC);
        }
    }

    //=============================================================================

    //todo:优化性能
    public void onTextChanged(String text)
    {
        currentText.setValue(text);
    }


    public void onInit(boolean type)
    {
        instanceType.setValue(type);
    }

    public void setFileUriPath(Uri uri){
        currentFileUri.setValue(uri);
    }
    public Uri getFileUriPath(){
        return currentFileUri.getValue();
    }

    public void setInstanceStatus(boolean status){
        instanceType.setValue(status);
    }

    public boolean getInstanceStatus(){
        return instanceType.getValue();
    }

    public void changeSaveStatus(boolean status){
        hasEdited.setValue(status);
    }

    public boolean getSaveStatus(){
        return hasEdited.getValue();
    }
}