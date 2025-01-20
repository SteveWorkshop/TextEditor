package io.github.materialapps.texteditor.logic.entity.vo;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class NoteVO implements Serializable {
    public Long id;
    public Long tagId;
    public String title;
    public String content;
    public String absc;//仅用于preview，空间换时间（其实是我懒doge）
    public String tagName;
    public int color;
    public Long createTime;
    public Long updateTime;
}
