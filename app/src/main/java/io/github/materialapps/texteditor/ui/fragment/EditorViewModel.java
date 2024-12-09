package io.github.materialapps.texteditor.ui.fragment;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.github.materialapps.texteditor.BaseApplication;
import lombok.Getter;
import lombok.Setter;

public class EditorViewModel extends ViewModel {

    @Getter
    @Setter
    private MutableLiveData<Boolean> show2Panel=new MutableLiveData<>(true);

    @Getter
    @Setter
    private MutableLiveData<Boolean> markdownMode=new MutableLiveData<>(true);


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