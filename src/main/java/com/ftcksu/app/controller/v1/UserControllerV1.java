package com.ftcksu.app.controller.v1;

import com.ftcksu.app.model.entity.*;
import com.ftcksu.app.model.request.PushNotificationRequest;
import com.ftcksu.app.model.response.PushNotificationResponse;
import com.ftcksu.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/users")
@Deprecated
public class UserControllerV1 {

    private final UserService userService;

    private final JobService jobService;

    private final EventService eventService;

    private final StorageService storageService;

    private final PushNotificationService pushNotificationService;

    @Autowired
    public UserControllerV1(UserService userService,
                            JobService jobService,
                            EventService eventService,
                            StorageService storageService,
                            PushNotificationService pushNotificationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.eventService = eventService;
        this.storageService = storageService;
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping
    public List<User> getAll(@RequestParam(name = "include_hidden", defaultValue = "false") boolean includeHidden) {
        return userService.getAllUsers(includeHidden);
    }

    @PostMapping
    public String addUser(@RequestBody User user) {
        try {
            userService.createNewUser(user);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "User saved.";
    }

    @GetMapping(value = "/{id}")
    public User getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> payload)
            throws InvocationTargetException, IllegalAccessException {
        userService.updateUser(id, payload);
        return "User updated.";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "User deleted.";
    }

    @GetMapping("/{id}/jobs")
    public List<Job> getJobs(@PathVariable Integer id) {
        return jobService.getJobsByUser(new User(id));
    }

    @PostMapping("/{id}/jobs/admin-submit")
    public String submitAdminJob(@PathVariable Integer id, @RequestBody Task task) {
        jobService.addTaskToAdminJob(id, task);
        return "Admin job submitted.";
    }

    @GetMapping("/{id}/events")
    public List<Event> getEvents(@PathVariable Integer id,
                                 @RequestParam(name = "leader", defaultValue = "false") Boolean leader) {
        return eventService.getEventsByUser(id, leader);
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity notifyUser(@PathVariable Integer id, @RequestBody PushNotificationRequest request) {
        String deviceToken = userService.getUserById(id).getDeviceToken();
        if (deviceToken == null) {
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.BAD_REQUEST.value(),
                    "Notification has failed to send."), HttpStatus.BAD_REQUEST);
        }
        request.setToken(userService.getUserById(id).getDeviceToken());
        pushNotificationService.sendPushNotificationToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(),
                "Notification has been sent."), HttpStatus.OK);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> loadImage(@PathVariable Integer id) {
        Resource resource = storageService.getUserImage(id, false);
        String resourceName = resource == null ? "default.jpg" : resource.getFilename();

        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceName + "\"")
                .contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @PutMapping("/{id}/image")
    public String updateImage(@PathVariable Integer id,
                              @RequestParam(name = "image_id") Integer imageId) {
        return userService.updateProfileImage(id, imageId) ? "Image changed successfully." : "Failed to change image.";
    }

    @GetMapping("/{id}/thumb")
    public ResponseEntity<Resource> loadThumbnail(@PathVariable Integer id) {
        Resource resource = storageService.getUserImage(id, true);
        String resourceName = resource == null ? "default.jpg" : resource.getFilename();

        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceName + "\"")
                .contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @GetMapping("/{id}/image/history")
    public List<ProfileImage> getImageHistor(@PathVariable Integer id) {
        return userService.getUserById(id).getImageHistory();
    }

    @GetMapping("/uotm")
    public List<?> getUserOTM(@RequestParam(name = "start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                              @RequestParam(name = "end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return jobService.getUserOTM(startDate, endDate);
    }

}
