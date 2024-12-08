package io.github.materialapps.texteditor.ui.fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import lombok.Getter;
import lombok.Setter;

public class EditorViewModel extends ViewModel {

    @Getter
    @Setter
    private MutableLiveData<String> currentText=new MutableLiveData<>();

    @Getter
    @Setter
    private MutableLiveData<Boolean> instanceType=new MutableLiveData<>();

    @Getter
    @Setter
    private MutableLiveData<Boolean> hasEdited=new MutableLiveData<>();

    @Getter
    @Setter
    private LiveData<Boolean> needToSave;

    @Getter
    @Setter
    private MutableLiveData<String> filePath;

    //todo:优化性能
    public void onTextChanged(String text)
    {
        currentText.setValue(text);
    }

    public void onInit(boolean type)
    {
        instanceType.setValue(type);
    }
}