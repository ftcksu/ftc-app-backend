package com.ftcksu.app.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AcceptedResponse<T> {
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "Asia/Riyadh")
    @CreationTimestamp
    private Date timestamp = new Date();
    private Integer status = HttpStatus.OK.value();
    private String message;
    private T result;

    public AcceptedResponse(String message) {
        this.message = message;
    }

    public AcceptedResponse(T result) {
        this.result = result;
    }

    public AcceptedResponse(String message, T result) {
        this.message = message;
        this.result = result;
    }
}
