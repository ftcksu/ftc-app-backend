package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.dto.EventDto;
import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.model.request.PushNotificationRequest;
import com.ftcksu.app.model.response.ResponseTemplate;
import com.ftcksu.app.service.EventService;
import com.ftcksu.app.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping(value = "/events")
@Slf4j
public class EventController {

    private final EventService eventService;

    private final PushNotificationService pushNotificationService;

    @Autowired
    public EventController(EventService eventService,
                           PushNotificationService pushNotificationService) {
        this.eventService = eventService;
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(new ResponseTemplate<>(eventService.getAllEvents()));
    }

    @PostMapping
    public ResponseEntity<?> addEvent(@RequestBody @Valid EventDto eventDto) {
        return ResponseEntity.ok(new ResponseTemplate<>("Event added successfully.", eventService.createNewEvent(eventDto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(eventService.getEventById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id, @RequestBody @Valid EventDto eventDto)
            throws InvocationTargetException, IllegalAccessException, ParseException {
        return ResponseEntity.ok(new ResponseTemplate<>("Event updated successfully.", eventService.updateEvent(id, eventDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>("Event deleted successfully.", eventService.deleteEvent(id)));
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<?> getEventJobs(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(eventService.getJobsByEvent(new Event(id))));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<?> getEventUsers(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(eventService.getEventById(id).getUsers()));
    }

    @PostMapping("/{id}/user")
    public ResponseEntity<?> addUserToEvent(@PathVariable Integer id, @RequestParam(value = "user_id") Integer userId) {
        return ResponseEntity.ok(new ResponseTemplate<>("User added successfully.", eventService.addUserToEvent(id, userId)));
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<?> addUsersToEvent(@PathVariable Integer id, @RequestBody List<User> users) {
        int addedUsers = 0;
        for (User user : users) {
            try {
                eventService.addUserToEvent(id, user.getId());
                addedUsers++;
            } catch (EntityExistsException ex) {
                log.error(ex.getMessage());
            }
        }
        return ResponseEntity.ok(new ResponseTemplate<>(addedUsers + " users added successfully.", addedUsers));
    }

    @PostMapping("/{id}/broadcast")
    public ResponseEntity broadcastToEventUsers(@PathVariable Integer id, @RequestBody PushNotificationRequest request) {
        Event eventToBroadcast = eventService.getEventById(id);
        List<String> deviceTokens = eventService.getUsersDeviceTokensByEvent(id);
        request.setTitle(eventToBroadcast.getTitle());
        request.setTokens(deviceTokens);
        return pushNotificationService.sendPushNotificationToMultipleTokens(request);
    }

    @DeleteMapping("/{id}/users")
    public ResponseEntity<?> removeUserFromEvent(@PathVariable Integer id, @RequestParam("user_id") Integer userId) {
        return ResponseEntity.ok(new ResponseTemplate<>("User removed successfully.", eventService.removeUser(id, userId)));
    }

}
