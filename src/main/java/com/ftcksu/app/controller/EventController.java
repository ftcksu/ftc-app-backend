package com.ftcksu.app.controller;

import com.ftcksu.app.model.dto.EventDto;
import com.ftcksu.app.model.dto.TaskDto;
import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.request.PushNotificationRequest;
import com.ftcksu.app.model.response.ResponseTemplate;
import com.ftcksu.app.service.EventService;
import com.ftcksu.app.service.JobService;
import com.ftcksu.app.service.PushNotificationService;
import com.ftcksu.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping(value = "/events")
@Slf4j
public class EventController {
    private final EventService eventService;
    private final JobService jobService;
    private final PushNotificationService pushNotificationService;
    private final UserService userService;

    @Autowired
    public EventController(EventService eventService,
                           JobService jobService,
                           PushNotificationService pushNotificationService,
                           UserService userService) {
        this.eventService = eventService;
        this.jobService = jobService;
        this.pushNotificationService = pushNotificationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(new ResponseTemplate<>(eventService.getAllEvents()));
    }

    @PostMapping
    public ResponseEntity<?> addEvent(@AuthenticationPrincipal UserDetails principal, @RequestBody @Valid EventDto eventDto,
                                      @RequestParam(name = "notify_users", defaultValue = "false") boolean notifyUsers) {

        Integer userId = Integer.parseInt(principal.getUsername());
        Event createdEvent = eventService.createNewEvent(userId, eventDto);

        if (notifyUsers) {
            List<String> deviceTokens = userService.getUsersDeviceTokens();
            PushNotificationRequest request = new PushNotificationRequest(createdEvent.getTitle(),
                    deviceTokens);
            pushNotificationService.sendPushNotificationToMultipleTokens(request);
        }

        return ResponseEntity.ok(new ResponseTemplate<>("Event added successfully.", createdEvent));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(eventService.getEventById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id, @RequestBody @Valid EventDto eventDto)
            throws InvocationTargetException, IllegalAccessException {
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

    @PutMapping("/{id}/jobs")
    public ResponseEntity<?> updateEventTask(@PathVariable Integer id,
                                             @RequestParam(name = "task_id") Integer taskId,
                                             @RequestParam(name = "approval_status") ApprovalStatus approvalStatus)
            throws InvocationTargetException, IllegalAccessException {
        TaskDto taskDto = TaskDto.builder()
                .approvalStatus(approvalStatus.equals(ApprovalStatus.READY) ? ApprovalStatus.READY : ApprovalStatus.UNAPPROVED)
                .build();
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.updateTask(taskId, taskDto)));
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
    public ResponseEntity<?> addUsersToEvent(@PathVariable Integer id, @RequestBody List<Integer> users) {
        int addedUsers = 0;
        for (Integer userId : users) {
            try {
                eventService.addUserToEvent(id, userId);
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
