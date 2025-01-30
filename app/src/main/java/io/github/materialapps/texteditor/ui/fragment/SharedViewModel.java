package io.github.materialapps.texteditor.ui.fragment;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.config.DBConfig;
import io.github.materialapps.texteditor.logic.dao.NoteDao;
import io.github.materialapps.texteditor.logic.entity.vo.NoteVO;
import lombok.Getter;
import lombok.Setter;

public class SharedViewModel extends ViewModel {

    private NoteDao noteDao;

    @Getter
    @Setter
    private MutableLiveData<Integer> currentIndex=new MutableLiveData<>(-1);

    @Getter
    @Setter
    private MutableLiveData<Boolean> requireNewNote=new MutableLiveData<>(false);

    @Getter
    @Setter
    private MutableLiveData<NoteVO> currentNote=new MutableLiveData<>();

    public SharedViewModel() { init(); }
    public SharedViewModel(LifecycleObserver observer){ init(); }

    private void init(){
        noteDao=DBConfig.getInstance(BaseApplication.getApplication()).getNoteDao();
    }


}
