package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;


@Data
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
    private String deviceToken;

    public void setStudentId(Integer id) {
        this.id = id;
    }
}
