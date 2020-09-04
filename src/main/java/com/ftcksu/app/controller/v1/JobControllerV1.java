package com.ftcksu.app.controller.v1;

import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.JobType;
import com.ftcksu.app.model.entity.Task;
import com.ftcksu.app.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/jobs")
@Deprecated
public class JobControllerV1 {

    private final JobService jobService;

    @Autowired
    public JobControllerV1(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<Job> getAllJobsByType(@RequestParam(name = "job_type", defaultValue = "ADMIN") JobType jobType) {
        return jobService.getJobsByType(jobType);
    }

    @PostMapping
    public String addJob(@RequestBody Job job) {
        jobService.createNewJob(job);
        return "Job saved.";
    }

    @GetMapping("/{id}")
    public Job getJob(@PathVariable Integer id) {
        return jobService.getJobById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteJob(@PathVariable Integer id) {
        jobService.deleteJob(id);
        return "Job deleted.";
    }

    @GetMapping("/{id}/tasks")
    public List<Task> getTasksByJob(@PathVariable Integer id) {
        return jobService.getTasksByJob(id);
    }

    @PostMapping("/{id}/tasks")
    public String addTaskToJob(@PathVariable Integer id, @RequestBody Task task) {
        jobService.addTaskToJob(id, task);
        return "Task saved.";
    }

}
