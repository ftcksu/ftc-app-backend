package com.ftcksu.app.controller.v1;

import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.entity.Task;
import com.ftcksu.app.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/tasks")
@Deprecated
public class TaskControllerV1 {

    private final JobService jobService;

    @Autowired
    public TaskControllerV1(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<Task> getTasksWithApprovalStatus(@RequestParam(name = "approval_status", defaultValue = "READY") ApprovalStatus approvalStatus) {
        return jobService.getTasksByApprovalStatus(approvalStatus);
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable Integer id) {
        return jobService.getTaskById(id);
    }

    @PutMapping("/{id}")
    public String updateTask(@PathVariable Integer id, @RequestBody Map<String, Object> payload)
            throws InvocationTargetException, IllegalAccessException {
        jobService.updateTask(id, payload);
        return "Task updated successfully.";
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Integer id) {
        jobService.deleteTask(id);
        return "Task deleted.";
    }

}
