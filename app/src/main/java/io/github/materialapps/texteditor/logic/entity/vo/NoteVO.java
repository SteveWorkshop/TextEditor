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
    public String tagName;
    public Long createTime;
    public Long updateTime;
}
