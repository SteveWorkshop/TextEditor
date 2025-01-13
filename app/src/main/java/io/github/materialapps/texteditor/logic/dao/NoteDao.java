package io.github.materialapps.texteditor.logic.dao;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import java.util.List;

import io.github.materialapps.texteditor.logic.entity.Note;
import io.github.materialapps.texteditor.logic.entity.vo.NoteVO;

@Dao
public interface NoteDao {
    @Insert
    Long insertNote(Note note);

    @Update
    int updateNote(Note note);

    @Deprecated
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select id,title,tag,createTime,updateTime from Note order by updateTime desc")
    List<Note> getAllPreview();

    @Deprecated
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select Note.id as id,Note.title,Note.createTime,Note.updateTime,Tag.id as tagId,Tag.tagName from Note left join Tag on Note.tag=Tag.id order by Note.updateTime desc")
    List<NoteVO> getAllPreview_V2();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select Note.id as id,Note.title,Note.createTime,Note.updateTime,Tag.id as tagId,Tag.tagName from Note left join Tag on Note.tag=Tag.id order by Note.updateTime desc")
    DataSource.Factory<Integer,NoteVO> getPreviewByPage();

    @Query("select Note.id as id,Note.title,Note.content,Note.createTime,Note.updateTime,Tag.id as tagId,Tag.tagName from Note left join Tag on Note.tag=Tag.id where Note.id=:id")
    NoteVO getById_V2(Long id);

    @Deprecated
    @Query("select * from Note where id=:id")
    Note getById(Long id);

    @Query("update Note set isDeleted=1 where id=:id")
    int deleteById(Long id);

    @Query("update Note set isDeleted=0 where id=:id")
    int recycleById(Long id);

    @Query("delete from Note where id=:id")
    int eraseById(Long id);

    @Query("select count (*) from Note where tag=:tid")
    int getCount(Long tid);
}
