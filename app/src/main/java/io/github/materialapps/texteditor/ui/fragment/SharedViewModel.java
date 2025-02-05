package io.github.materialapps.texteditor.ui.fragment;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import io.github.materialapps.texteditor.BaseApplication;
import io.github.materialapps.texteditor.config.DBConfig;
import io.github.materialapps.texteditor.logic.dao.NoteDao;
import io.github.materialapps.texteditor.logic.dao.TagDao;
import io.github.materialapps.texteditor.logic.entity.Note;
import io.github.materialapps.texteditor.logic.entity.Tag;
import io.github.materialapps.texteditor.logic.entity.vo.NoteVO;
import lombok.Getter;
import lombok.Setter;

public class SharedViewModel extends ViewModel {

    private static final String TAG = "SharedViewModel";

    private NoteDao noteDao;
    private TagDao tagDao;

    @Getter
    @Setter
    private boolean dbMode=false;

    @Getter
    @Setter
    private boolean addMode=true;

    @Getter
    @Setter
    private boolean isModified=false;

    @Getter
    @Setter
    private boolean isTagModified=false;

    @Getter
    @Setter
    private Long currentNoteId =-1l;

    @Getter
    @Setter
    private int currentTagIndex=-1;

    @Getter
    @Setter
    private Long currentTid=-1l;

    @Getter
    @Setter
    private MutableLiveData<NoteVO> currentNote=new MutableLiveData<>();

    @Getter
    @Setter
    private MutableLiveData<Void> newStickyTrigger=new MutableLiveData<>();

    @Getter
    @Setter
    private List<Tag> tags;

    public SharedViewModel() { init(); }
    public SharedViewModel(LifecycleObserver observer){ init(); }

    private void init(){
        noteDao=DBConfig.getInstance(BaseApplication.getApplication()).getNoteDao();
        tagDao=DBConfig.getInstance(BaseApplication.getApplication()).getTagDao();

        //预读标签列表
        //todo:在这么小的空间做分页emmm
        tags=tagDao.getAll();
    }

    public void triggerNewNote(){
        Log.d(TAG, "triggerNewNote: =========================");
        dbMode=true;
        addMode=true;
        isModified=false;
        isTagModified=false;
        currentNoteId =-1L;
        currentTagIndex=-1;
        currentTid=-1L;
        //我就是个触发器
        newStickyTrigger.setValue(newStickyTrigger.getValue());
    }

    public Long addNote(String content,long tagId){
        //提取摘要
        if(content!=null){
            List<String> infos = extractInfo(content);
            if(infos.size() == 2){
                String title=infos.get(0);
                String pumpingElephant=infos.get(1);
                Note note=new Note();
                note.setTitle(title);
                note.setAbsc(pumpingElephant);
                note.setContent(content);
                note.setTag(tagId);
                return noteDao.insertNote(note);
            }
        }
        return -1L;
    }

    public int updateNote(Long currentId,String content,long tagId){
        String title="空内容";
        String pumpingElephant="";
        if(!TextUtils.isEmpty(content)){
            List<String> infos = extractInfo(content);
            if(infos!=null&&infos.size()==2){
                title=infos.get(0);
                pumpingElephant=infos.get(1);
            }
        }
        else{
            content="";
        }
        Note note=new Note();
        note.setTitle(title);
        note.setAbsc(pumpingElephant);
        note.setContent(content);
        note.setTag(tagId);
        note.setId(currentId);

        return noteDao.updateNote(note);
    }

    private List<String> extractInfo(String content){
        List<String> ret=new ArrayList<>();
        if(TextUtils.isEmpty(content)){
            ret.add("空内容");
            ret.add("");
            return ret;
        }
        String title;
        int paraEnd = content.indexOf('\n');
        if(paraEnd==-1){
            int end=Math.min(10,content.length());
            title= content.substring(0,end);
        }
        else{
            String candidate=content.substring(0,paraEnd);
            int end=Math.min(20,candidate.length());
            title= candidate.substring(0,end);
        }
        int endP=Math.min(20,content.length());
        String pumpingElephant=content.substring(0,endP);

        ret.add(title);
        ret.add(pumpingElephant);
        return ret;
    }
}
