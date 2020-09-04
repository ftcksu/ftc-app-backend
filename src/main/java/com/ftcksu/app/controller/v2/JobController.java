package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.JobType;
import com.ftcksu.app.model.entity.Task;
import com.ftcksu.app.model.response.ResponseTemplate;
import com.ftcksu.app.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v2/jobs")
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
    public ResponseEntity<?> addJob(@RequestBody Job job) {
        jobService.createNewJob(job);
        return ResponseEntity.ok(new ResponseTemplate<>("Job saved successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJob(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.getJobById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Integer id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok(new ResponseTemplate<>("Job deleted successfully."));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<?> getTasksByJob(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseTemplate<>(jobService.getTasksByJob(id)));
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<?> addTaskToJob(@PathVariable Integer id, @RequestBody Task task) {
        jobService.addTaskToJob(id, task);
        return ResponseEntity.ok(new ResponseTemplate<>("Task saved successfully."));
    }

}
