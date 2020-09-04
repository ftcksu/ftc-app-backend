package com.ftcksu.app.service;

import com.ftcksu.app.firebase.FCMService;
import com.ftcksu.app.model.request.PushNotificationRequest;
import com.ftcksu.app.model.response.PushNotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PushNotificationService {

    private final FCMService fcmService;
    private final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    @Autowired
    public PushNotificationService(FCMService fcmService) {
        this.fcmService = fcmService;
    }


    public void sendPushNotification(Map<String, String> data, PushNotificationRequest request) {
        try {
            fcmService.sendMessage(data, request);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }


    public void sendPushNotificationWithoutData(PushNotificationRequest request) {
        try {
            fcmService.sendMessageWithoutData(request);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }


    public void sendPushNotificationToToken(PushNotificationRequest request) {
        try {
            fcmService.sendMessageToToken(request);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }


    public ResponseEntity sendPushNotificationToMultipleTokens(PushNotificationRequest request) {
        try {
            fcmService.sendMessageToMultipleTokens(request);
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(),
                    "Notification has been sent."), HttpStatus.OK);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.BAD_REQUEST.value(),
                    "Notification has failed to send."), HttpStatus.BAD_REQUEST);
        }
    }

}
