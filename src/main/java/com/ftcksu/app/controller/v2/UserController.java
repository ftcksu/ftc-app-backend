package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.entity.Task;
import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.model.request.PushNotificationRequest;
import com.ftcksu.app.model.response.AcceptedResponse;
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
import java.util.Map;

@RestController
@RequestMapping(value = "/v2/users")
public class UserController {

    private final UserService userService;

    private final JobService jobService;

    private final EventService eventService;

    private final StorageService storageService;

    private final PushNotificationService pushNotificationService;

    @Autowired
    public UserController(UserService userService,
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
    public ResponseEntity<?> getAll(@RequestParam(name = "include_hidden", defaultValue = "false") boolean includeHidden) {
        return ResponseEntity.ok(new AcceptedResponse<>(userService.getAllUsers(includeHidden)));
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User user) {
        userService.createNewUser(user);
        return ResponseEntity.ok(new AcceptedResponse<>("User Added Successfully."));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(new AcceptedResponse<>(userService.getUserById(id)));
    }

    //    TODO: check the header throws
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> payload)
            throws InvocationTargetException, IllegalAccessException {
        userService.updateUser(id, payload);
        return ResponseEntity.ok(new AcceptedResponse<>("User updated successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new AcceptedResponse<>("User deleted successfully."));
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<?> getJobs(@PathVariable Integer id) {
        return ResponseEntity.ok(new AcceptedResponse<>(jobService.getJobsByUser(new User(id))));
    }

    @PostMapping("/{id}/jobs/admin-submit")
    public ResponseEntity<?> submitAdminJob(@PathVariable Integer id, @RequestBody Task task) {
        jobService.addTaskToAdminJob(id, task);
        return ResponseEntity.ok(new AcceptedResponse<>("Admin job submitted successfully."));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getEvents(@PathVariable Integer id,
                                       @RequestParam(name = "leader", defaultValue = "false") Boolean leader) {
        return ResponseEntity.ok(new AcceptedResponse<>(eventService.getEventsByUser(id, leader)));
    }

    // TODO: check if it's possible to remove the if statement
    @PostMapping("/{id}/notify")
    public ResponseEntity<?> notifyUser(@PathVariable Integer id, @RequestBody PushNotificationRequest request) {
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
    public ResponseEntity<?> updateImage(@PathVariable Integer id,
                                         @RequestParam(name = "image_id") Integer imageId) {
        if (userService.updateProfileImage(id, imageId)) {
            return ResponseEntity.ok(new AcceptedResponse<>("Image changed successfully."));
        } else {
            return new ResponseEntity(new AcceptedResponse<>("Failed to change image."), HttpStatus.BAD_GATEWAY);
        }
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
    public ResponseEntity<?> getImageHistory(@PathVariable Integer id) {
        return ResponseEntity.ok(new AcceptedResponse<>(userService.getUserById(id).getImageHistory()));
    }

    @GetMapping("/uotm")
    public ResponseEntity<?> getUserOTM(@RequestParam(name = "start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                                        @RequestParam(name = "end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return ResponseEntity.ok(new AcceptedResponse<>(jobService.getUserOTM(startDate, endDate)));
    }

}
