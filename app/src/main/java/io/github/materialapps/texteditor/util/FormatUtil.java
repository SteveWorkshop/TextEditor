package io.github.materialapps.texteditor.util;

public class FormatUtil {
    public static final String BOLD_FORMAT_CONTROLLER="**";
    public static final String ITALIC_FORMAT_CONTROLLER="*";
    public static final String HEADER_FORMAT_CONTROLLER="#";
    public static final String UL_FORMAT_CONTROLLER="-";
    public static final String OL_FORMAT_CONTROLLER=".";
    public static final String SINGLE_CODE_FORMAT_CONTROLLER="`";

    public static String getBold(){
        return BOLD_FORMAT_CONTROLLER;
    }

    public static String getItalic(){
        return ITALIC_FORMAT_CONTROLLER;
    }

    public static String getHeader(int level){
        if(level<=0){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<level;i++){
            sb.append(HEADER_FORMAT_CONTROLLER);
        }
        sb.append(" ");
        return sb.toString();
    }

    public static String getUl(){
        return UL_FORMAT_CONTROLLER;
    }

    public static String getOl(int level){
        if(level<=0){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        sb.append(level);
        sb.append(OL_FORMAT_CONTROLLER);
        sb.append(" ");
        return sb.toString();
    }

    public static String getSingleCode(){
        return SINGLE_CODE_FORMAT_CONTROLLER;
    }
}
