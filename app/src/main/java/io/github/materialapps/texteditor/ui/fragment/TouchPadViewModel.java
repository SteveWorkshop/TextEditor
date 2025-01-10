package io.github.materialapps.texteditor.ui.fragment;

import android.graphics.Color;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.github.materialapps.texteditor.ui.flyout.CanvasFlyout;
import lombok.Getter;
import lombok.Setter;

public class TouchPadViewModel extends ViewModel {
    // TODO: Implement the ViewModel

    //todo:可设置
    @Getter
    @Setter
    private MutableLiveData<Integer> penMode=new MutableLiveData<>(CanvasFlyout.PEN_MODE);

    @Getter
    @Setter
    private MutableLiveData<Integer> penColor=new MutableLiveData<>(Color.CYAN);

    @Getter
    @Setter
    private MutableLiveData<Float> penStrokeSize=new MutableLiveData<>(5f);

    @Getter
    @Setter
    private boolean eraserMode;
}