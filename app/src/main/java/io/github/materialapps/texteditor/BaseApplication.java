package io.github.materialapps.texteditor;

import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

public class BaseApplication extends Application {
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

