package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.dto.TaskDto;
import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.response.ResponseTemplate;
import com.ftcksu.app.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping(value = "/tasks")
public class TaskController {

    private final JobService jobService;

    @Autowired
    public TaskController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<?> getTasksWithApprovalStatus(@RequestParam(name = "approval_status", defaultValue = "READY") ApprovalStatus approvalStatus) {
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.getTasksByApprovalStatus(approvalStatus)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.getTaskById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody @Valid TaskDto taskDto)
            throws InvocationTargetException, IllegalAccessException {
        return ResponseEntity.ok(new ResponseTemplate<>("Task updated successfully.",jobService.updateTask(id, taskDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>("Task deleted successfully.",jobService.deleteTask(id)));
    }

}
