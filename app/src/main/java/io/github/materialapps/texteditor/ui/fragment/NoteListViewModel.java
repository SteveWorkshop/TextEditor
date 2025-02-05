package io.github.materialapps.texteditor.ui.fragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.config.DBConfig;
import io.github.materialapps.texteditor.logic.dao.NoteDao;
import io.github.materialapps.texteditor.logic.entity.vo.NoteVO;
import lombok.Getter;
import lombok.Setter;

public class NoteListViewModel extends AndroidViewModel {
    // TODO: Implement the ViewModel

    private NoteDao noteDao;

    @Getter
    @Setter
    private LiveData allData;

    //private ExecutorService exec = Executors.newCachedThreadPool();

    public NoteListViewModel(@NonNull Application application) {
        super(application);
        init();
    }


    public NoteVO getById(Long id){
        return noteDao.getById_V2(id);
    }

    public int delete(Long id){
        return noteDao.deleteById(id);
    }

    private void init(){
        noteDao= DBConfig.getInstance(BaseApplication.getApplication()).getNoteDao();
        loadDataByPage();
    }

    private void loadDataByPage(){
        PagedList.Config.Builder builder=new PagedList.Config.Builder();
        builder.setPageSize(BaseApplication.PAGE_SIZE);                       //配置分页加载的数量
        builder.setEnablePlaceholders(BaseApplication.ENABLE_PLACEHOLDERS);     //配置是否启动PlaceHolders
        builder.setInitialLoadSizeHint(BaseApplication.PAGE_SIZE);
        LivePagedListBuilder livePagedListBuilder = new LivePagedListBuilder(noteDao.getPreviewByPage(), builder.build());
        allData=livePagedListBuilder.build();
    }
}