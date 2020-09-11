package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ftcksu.app.model.entity.ApprovalStatus;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDto {

    private Integer id;
    private String description;
    private int points;
    private ApprovalStatus approvalStatus = ApprovalStatus.READY;

}
