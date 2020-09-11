package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ftcksu.app.model.entity.JobType;
import com.ftcksu.app.model.entity.Task;
import com.ftcksu.app.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDto {

    private String title;
    private User user;
    private List<Task> tasks = new ArrayList<>();
    private JobType jobType = JobType.EVENT;

    public void setUserId(Integer userId) {
        user = new User(userId);
    }

}
