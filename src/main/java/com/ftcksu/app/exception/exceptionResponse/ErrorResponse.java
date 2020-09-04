package com.ftcksu.app.exception.exceptionResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "Asia/Riyadh")
    private Date timestamp;
    private Integer status;
    private String error;
    private String message;
    private List<SubError> errors;

    public ErrorResponse(String error) {
        this.error = error;
    }
}
