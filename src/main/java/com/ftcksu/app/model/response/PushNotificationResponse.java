package com.ftcksu.app.model.response;

import lombok.Data;

@Data
public class PushNotificationResponse {

    private int status;
    private String message;

    public PushNotificationResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
