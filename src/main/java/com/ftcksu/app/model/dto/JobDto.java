package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ftcksu.app.model.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDto {

    private String title;
    private Integer userId;
    private JobType jobType = JobType.EVENT;


}
