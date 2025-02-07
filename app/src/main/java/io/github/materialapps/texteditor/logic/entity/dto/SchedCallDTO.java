package io.github.materialapps.texteditor.logic.entity.dto;

import lombok.Data;

@Data
public class SchedCallDTO {
    private String title;
    private String content;
    private Long startTime;
    private Long endTime;
}
