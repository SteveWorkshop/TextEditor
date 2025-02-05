package io.github.materialapps.texteditor;

import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

public class BaseApplication extends Application {

    public static final boolean NEW_FILE=false;
    public static final boolean OPEN_FILE=true;

    public static final boolean MODIFIED=true;
    public static final boolean UNMODIFIED=false;

    public static final int EXTERNAL_EDIT_MODE=0;
    public static final int EXTERNAL_NEW_MODE=-1;
    public static final int DB_EDIT=1;

    public static final int MIN_UI_SIZE=5;
    public static final int MAX_UI_SIZE=72;

    public static final int PAGE_SIZE = 15;
    public static final  boolean ENABLE_PLACEHOLDERS = false;

    private static volatile Context context;

    public static synchronized Context getApplication()
    {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}

