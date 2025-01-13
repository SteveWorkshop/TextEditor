package io.github.materialapps.texteditor.logic.entity;

import androidx.room.Entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
public class Tag extends BaseEntity implements Serializable {
    public static final long DEFAULT_TAG=-1024L;
    private String tagName;
}
