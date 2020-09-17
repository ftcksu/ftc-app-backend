package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {
    private String title;
    private String description;
    private Date date;
    private String whatsAppLink;
    private Integer maxUsers;
    private String location;
    private boolean finished;
}
