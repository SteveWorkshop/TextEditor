package io.github.materialapps.texteditor.logic.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;
import androidx.paging.DataSource;

import java.util.List;

import io.github.materialapps.texteditor.logic.entity.Tag;

@Dao
public interface TagDao {
    @Insert
    Long insertTag(Tag tag);

    @Update
    int updateTag(Tag tag);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select id,tagName,color from Tag where id=:id and isDeleted=0")
    Tag getById(Long id);

    @Deprecated//暂时废掉
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select id,tagName,color from Tag where isDeleted!=1 order by updateTime desc")
    List<Tag> getAll();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select id,tagName,color from Tag where isDeleted!=1 order by updateTime desc")
    DataSource.Factory<Integer,Tag> getByPage();

    @Query("update Tag set isDeleted=1 where id=:id")
    int deleteById(Long id);

    @Query("update Tag set isDeleted=0 where id=:id")
    int recycleById(Long id);

    @Query("delete from Tag where id=:id")
    int eraseById(Long id);
}
