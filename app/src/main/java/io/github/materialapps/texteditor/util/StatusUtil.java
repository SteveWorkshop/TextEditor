package io.github.materialapps.texteditor.util;

import io.github.materialapps.texteditor.BaseApplication;

public class StatusUtil {
    public static final int NO_ACTION=0;
    public static final int NEED_TO_SAVE=1;
    public static final int NEED_TO_SAVE_AS=2;

    public static int checkSaveStatus(boolean newDocument, boolean changed){
        if(newDocument== BaseApplication.NEW_FILE && changed==BaseApplication.MODIFIED){
            return NEED_TO_SAVE_AS;
        }
        if(newDocument==BaseApplication.OPEN_FILE&&changed==BaseApplication.MODIFIED)
        {
            return NEED_TO_SAVE;
        }
        return NO_ACTION;
    }
}
