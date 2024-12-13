package io.github.materialapps.texteditor.util;

public class FormatUtil {
    public static final String BOLD_FORMAT_CONTROLLER="**";
    public static final String ITALIC_FORMAT_CONTROLLER="*";
    public static final String HEADER_FORMAT_CONTROLLER="#";
    public static final String UL_FORMAT_CONTROLLER="-";
    public static final String OL_FORMAT_CONTROLLER=".";
    public static final String SINGLE_CODE_FORMAT_CONTROLLER="`";
    public static final String LINE_FORMAT_CONTROLLER="---";
    public static final String TABLE_VERTICAL_FORMAT_CONTROLLER="|";
    public static final String TABLE_HEADER_DIVIDER_FORMAT_CONTROLLER="-----";
    public static final String TABLE_CELL_PLACEHOLDER_FORMAT_CONTROLLER="    ";

    public static final String WARNING_DIVIDER="<!-- 系统生成数据，请勿编辑！ -->";
    public static final String IMG_INLINE_START_DIVIDER="<!-- 图片数据起始 -->";
    public static final String IMG_INLINE_END_DIVIDER="<!-- 图片数据起始 -->";

    public static final String TAG_DEFAULT="图片描述";

    public static final String IMG_TAG_HEAD_FORMAT_CONTROLLER="![";
    public static final String IMG_TAG_CLOSE_FORMAT_CONTROLLER="]";

    public static final String TMG_DATA_INLINE_HEAD_FORMAT_CONTROLLER="[";
    public static final String TMG_DATA_INLINE_CLOSE_FORMAT_CONTROLLER="]";

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
        return UL_FORMAT_CONTROLLER+" ";
    }

    public static String getOl(int num){
        if(num<=0){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        sb.append(num);
        sb.append(OL_FORMAT_CONTROLLER);
        sb.append(" ");
        return sb.toString();
    }

    public static String getSingleCode(){
        return SINGLE_CODE_FORMAT_CONTROLLER;
    }

    public static String getLine(){
        return LINE_FORMAT_CONTROLLER;
    }

    public static String getTableFrame(int row,int col){
        if(row<2&&col<2){return "";}//markdown不支持单行单列表格
        StringBuilder sb=new StringBuilder();
        //渲染第一行
        String row1=getTableRow(col,TABLE_CELL_PLACEHOLDER_FORMAT_CONTROLLER);
        sb.append(row1);
        //渲染分隔符
        String rowd=getTableRow(col,TABLE_HEADER_DIVIDER_FORMAT_CONTROLLER);
        sb.append(rowd);
        //渲染剩下的内容
        for(int i=1;i<row;i++){
            String rowi=getTableRow(col,TABLE_CELL_PLACEHOLDER_FORMAT_CONTROLLER);
            sb.append(rowi);
        }
        return sb.toString();
    }

    private static String getTableRow(int col,String fill){
        if(col<2){return "";}
        StringBuilder l1=new StringBuilder();
        l1.append(TABLE_VERTICAL_FORMAT_CONTROLLER);
        for(int i=0;i<col;i++){
            l1.append(fill);
            l1.append(TABLE_VERTICAL_FORMAT_CONTROLLER);
        }
        l1.append("\n");
        return l1.toString();
    }
}
