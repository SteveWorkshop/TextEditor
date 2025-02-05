package io.github.materialapps.texteditor.ui.fragment;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.config.DBConfig;
import io.github.materialapps.texteditor.logic.dao.TagDao;
import io.github.materialapps.texteditor.logic.entity.Tag;
import lombok.Getter;
import lombok.Setter;

public class TagListViewModel extends ViewModel {

    @Getter
    @Setter
    private TagDao tagDao;

    @Getter
    @Setter
    private LiveData allData;

    private ExecutorService exec = Executors.newSingleThreadExecutor();

    @Getter
    @Setter
    private MutableLiveData<Boolean> loading=new MutableLiveData<>(false);

    @Getter
    @Setter
    private MutableLiveData<Boolean> faliure=new MutableLiveData<>(false);

    public TagListViewModel(){init();}
    public TagListViewModel(LifecycleObserver observer){init();}

    private void init(){
        tagDao= DBConfig.getInstance(BaseApplication.getApplication()).getTagDao();
        loadDataByPage();
    }

    private void loadDataByPage(){
        PagedList.Config.Builder builder=new PagedList.Config.Builder();
        builder.setPageSize(BaseApplication.PAGE_SIZE);                       //配置分页加载的数量
        builder.setEnablePlaceholders(BaseApplication.ENABLE_PLACEHOLDERS);     //配置是否启动PlaceHolders
        builder.setInitialLoadSizeHint(BaseApplication.PAGE_SIZE);
        LivePagedListBuilder livePagedListBuilder = new LivePagedListBuilder(tagDao.getByPage(), builder.build());
        allData=livePagedListBuilder.build();
    }

    public void addTag(Tag tag){
        loading.setValue(true);
        exec.execute(()->{
            Long ret = tagDao.insertTag(tag);
            loading.postValue(false);
            if(ret>0){
                //成功

            }
            else {
                faliure.postValue(true);
            }
        });
    }

    public void deleteTag(Long id){
        if(id<0){return;}
        loading.setValue(true);
        exec.execute(()->{
            int ret=tagDao.deleteById(id);
            loading.postValue(false);
            if(ret>0){
                //成功

            }
            else {
                faliure.postValue(true);
            }
        });
    }

    public void updateTag(Tag tag){
        loading.setValue(true);
        exec.execute(()->{
            int ret=tagDao.updateTag(tag);
            loading.postValue(false);
            if(ret>0){
                //成功

            }
            else {
                faliure.postValue(true);
            }
        });
    }
}