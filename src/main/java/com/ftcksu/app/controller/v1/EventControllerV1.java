package com.ftcksu.app.controller.v1;

import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.model.request.PushNotificationRequest;
import com.ftcksu.app.service.EventService;
import com.ftcksu.app.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/events")
@Deprecated
public class EventControllerV1 {

    private final EventService eventService;

    private final PushNotificationService pushNotificationService;

    @Autowired
    public EventControllerV1(EventService eventService,
                             PushNotificationService pushNotificationService) {
        this.eventService = eventService;
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping
    public List<Event> getAll() {
        return eventService.getAllEvents();
    }

    @PostMapping
    public Event addEvent(@RequestBody Event event) {
        return eventService.createNewEvent(event);
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable Integer id) {
        return eventService.getEventById(id);
    }

    @PutMapping("/{id}")
    public String updateEvent(@PathVariable Integer id, @RequestBody Map<String, Object> payload)
            throws InvocationTargetException, IllegalAccessException, ParseException {
        eventService.updateEvent(id, payload);
        return "Event updated successfully.";
    }

    @DeleteMapping("/{id}")
    public String deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
        return "Event deleted.";
    }

    @GetMapping("/{id}/jobs")
    public List<Job> getEventJobs(@PathVariable Integer id) {
        return eventService.getJobsByEvent(new Event(id));
    }

    @GetMapping("/{id}/users")
    public Set<User> getEventUsers(@PathVariable Integer id) {
        return eventService.getEventById(id).getUsers();
    }

    @PostMapping("/{id}/users")
    public String addUserToEvent(@PathVariable Integer id,
                                 @RequestParam(value = "user_id", defaultValue = "-1") Integer userId,
                                 @RequestBody(required = false) List<User> users) {
        if (users != null) {
            users.forEach(user -> eventService.addUserToEvent(id, user.getId()));
            return "Users added.";
        } else {
            return eventService.addUserToEvent(id, userId) ? "User added successfully." : "Failed to add user.";
        }
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
    public String removeUserFromEvent(@PathVariable Integer id, @RequestParam("user_id") Integer userId) {
        eventService.removeUser(id, userId);
        return "User removed.";
    }

}
