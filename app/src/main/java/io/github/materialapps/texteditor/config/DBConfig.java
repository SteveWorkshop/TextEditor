package io.github.materialapps.texteditor.config;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.github.materialapps.texteditor.logic.dao.NoteDao;
import io.github.materialapps.texteditor.logic.dao.TagDao;
import io.github.materialapps.texteditor.logic.entity.Note;
import io.github.materialapps.texteditor.logic.entity.Tag;

@Database(version = 1,entities ={Note.class, Tag.class}, exportSchema=false)
public abstract class DBConfig extends RoomDatabase {
    public static final String DB_NAME="yuri.db";
    private static volatile DBConfig instance;
    public static synchronized DBConfig getInstance(Context context)
    {
        if(instance==null)
        {
            instance=create(context.getApplicationContext());
        }
        return instance;
    }

    private static DBConfig create(final Context context)
    {
        return Room.databaseBuilder(context, DBConfig.class,DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public abstract NoteDao getNoteDao();
    public abstract TagDao getTagDao();
}
