package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.dto.JobDto;
import com.ftcksu.app.model.dto.TaskDto;
import com.ftcksu.app.model.entity.JobType;
import com.ftcksu.app.model.response.ResponseTemplate;
import com.ftcksu.app.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/jobs")
public class JobController {

    private final JobService jobService;

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<?> getAllJobsByType(@RequestParam(name = "job_type", defaultValue = "ADMIN") JobType jobType) {
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.getJobsByType(jobType)));
    }

    @PostMapping
    public ResponseEntity<?> addJob(@RequestBody @Valid JobDto jobDto) {
        return ResponseEntity.ok(new ResponseTemplate<>("Job saved successfully.", jobService.createNewJob(jobDto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJob(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.getJobById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>("Job deleted successfully.", jobService.deleteJob(id)));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<?> getTasksByJob(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.getTasksByJob(id)));
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<?> addTaskToJob(@PathVariable Integer id, @RequestBody @Valid TaskDto taskDto) {
        return ResponseEntity.ok(new ResponseTemplate<>("Task saved successfully.", jobService.addTaskToJob(id, taskDto)));
    }
}
