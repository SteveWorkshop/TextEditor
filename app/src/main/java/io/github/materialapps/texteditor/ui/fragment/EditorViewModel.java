package io.github.materialapps.texteditor.ui.fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import lombok.Getter;
import lombok.Setter;

public class EditorViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    @Getter
    @Setter
    private MutableLiveData<String> currentText=new MutableLiveData<>();

    //todo:优化性能
    public void onTextChanged(String text)
    {
        currentText.setValue(text);
    }
}