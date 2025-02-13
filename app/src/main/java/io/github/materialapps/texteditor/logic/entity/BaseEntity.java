package io.github.materialapps.texteditor.logic.entity;

import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Boolean isDeleted=false;
    private Long createTime=System.currentTimeMillis();
    private Long updateTime=System.currentTimeMillis();
}
