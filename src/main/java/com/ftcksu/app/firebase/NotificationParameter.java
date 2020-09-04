package com.ftcksu.app.firebase;

public enum NotificationParameter {

    SOUND("default"),
    COLOR("#000000");

    private String value;

    NotificationParameter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
