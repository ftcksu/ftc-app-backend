package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.response.AcceptedResponse;
import com.ftcksu.app.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@RestController
@RequestMapping(value = "/v2/tasks")
public class TaskController {

    private final JobService jobService;

    @Autowired
    public TaskController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<?> getTasksWithApprovalStatus(@RequestParam(name = "approval_status", defaultValue = "READY") ApprovalStatus approvalStatus) {
        return ResponseEntity.ok(new AcceptedResponse<>(jobService.getTasksByApprovalStatus(approvalStatus)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Integer id) {
        return ResponseEntity.ok(new AcceptedResponse<>(jobService.getTaskById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody Map<String, Object> payload)
            throws InvocationTargetException, IllegalAccessException {
        jobService.updateTask(id, payload);
        return ResponseEntity.ok(new AcceptedResponse<>("Task updated successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        jobService.deleteTask(id);
        return ResponseEntity.ok(new AcceptedResponse<>("Task deleted successfully."));
    }

}
