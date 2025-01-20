package io.github.materialapps.texteditor.logic.entity;

import androidx.room.Entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Entity
public class Note extends BaseEntity implements Serializable {
    private String title;//todo:title废弃，改用第一行取缩写
    private String content;
    private String absc;
    private Long tag=Tag.DEFAULT_TAG;
}
