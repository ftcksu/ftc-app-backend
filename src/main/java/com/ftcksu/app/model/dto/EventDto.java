package com.ftcksu.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ftcksu.app.model.entity.User;
import lombok.*;

import javax.validation.constraints.Pattern;
import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {

    private String title;
    private String description;
    private Date date;
    @Pattern(regexp = ".*chat.whatsapp.com/.*",message = "Invalid whatsapp link")
    private String whatsAppLink;
    private int maxUsers;
    private String location;
    private User leader;

}
