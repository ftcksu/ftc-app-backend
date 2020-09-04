package com.ftcksu.app.controller.v1;

import com.ftcksu.app.model.request.PushNotificationRequest;
import com.ftcksu.app.model.response.PushNotificationResponse;
import com.ftcksu.app.service.PushNotificationService;
import com.ftcksu.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/notifications")
@Deprecated
public class PushNotificationControllerV1 {

    private final PushNotificationService pushNotificationService;

    private final UserService userService;

    @Autowired
    public PushNotificationControllerV1(PushNotificationService pushNotificationService,
                                        UserService userService) {
        this.pushNotificationService = pushNotificationService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity broadcastNotification(@RequestBody PushNotificationRequest request) {
        List<String> deviceTokens = userService.getUsersDeviceTokens();
        request.setTokens(deviceTokens);
        return pushNotificationService.sendPushNotificationToMultipleTokens(request);
    }

    @PostMapping("/topic")
    public ResponseEntity sendTopicNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationWithoutData(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(),
                "Notification has been sent."), HttpStatus.OK);
    }

    @PostMapping("/token")
    public ResponseEntity sendTokenNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(),
                "Notification has been sent."), HttpStatus.OK);
    }

}
