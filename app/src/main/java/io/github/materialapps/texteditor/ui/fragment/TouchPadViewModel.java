package io.github.materialapps.texteditor.ui.fragment;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import io.github.materialapps.texteditor.R;
import io.github.materialapps.texteditor.ui.flyout.CanvasFlyout;
import lombok.Getter;
import lombok.Setter;

public class TouchPadViewModel extends AndroidViewModel {
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
    private MutableLiveData<Integer> inputMode=new MutableLiveData<>(CanvasFlyout.TOUCH_MODE);

    @Getter
    @Setter
    private boolean eraserMode;

    @Getter
    @Setter
    private SharedPreferences spf;

    public TouchPadViewModel(@NonNull Application application) {
        super(application);
        spf = PreferenceManager.getDefaultSharedPreferences(application);
        String paintMode = spf.getString("paint_mode", CanvasFlyout.TOUCH_MODE+"");
        inputMode.setValue( Integer.parseInt(paintMode));
    }
}