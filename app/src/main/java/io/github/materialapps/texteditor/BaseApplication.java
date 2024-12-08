package io.github.materialapps.texteditor;

import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

public class BaseApplication extends Application {

    public static final boolean NEW_FILE=false;
    public static final boolean OPEN_FILE=true;

    public static final boolean MODIFIED=true;
    public static final boolean UNMODIFIED=false;

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

