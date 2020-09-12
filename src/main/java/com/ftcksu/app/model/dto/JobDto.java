package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ftcksu.app.model.entity.JobType;
import com.ftcksu.app.model.entity.Task;
import com.ftcksu.app.model.entity.User;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
