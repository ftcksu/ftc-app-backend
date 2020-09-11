package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.Size;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Integer id;
    private String name;
    private String phoneNumber;
    @Size(min = 8, message = "password must be 8 or more characters in length.")
    private String password;
    private String bio;
    private boolean hidden;
    private String deviceToken;

    public void setStudentId(Integer id) {
        this.id = id;
    }
}
