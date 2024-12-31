package io.github.materialapps.texteditor.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import lombok.Getter;
import lombok.Setter;

public class MainViewModel extends AndroidViewModel {

    @Getter
    @Setter
    private MutableLiveData<Boolean> sideBarStatus=new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }
}
