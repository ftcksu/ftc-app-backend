package com.ftcksu.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftcksu.app.model.dto.TaskDto;
import com.ftcksu.app.model.entity.*;
import com.ftcksu.app.repository.JobRepository;
import com.ftcksu.app.repository.TaskRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JobService {


    // TODO: Change void to boolean to check if the method worked.

    private final JobRepository jobRepository;

    private final TaskRepository taskRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;

    private  final ObjectMapper objectMapper;

    @Autowired
    public JobService(JobRepository jobRepository, TaskRepository taskRepository, UserService userService) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.modelMapper = new ModelMapper();
        this.objectMapper = new ObjectMapper();
    }


    public List<Job> getJobsByUser(User user) {
        return jobRepository.findJobsByUserEqualsOrderByUpdatedAtDesc(user);
    }


    public List<Job> getJobsByType(JobType jobType) {
        return jobRepository.findJobsByJobType(jobType);
    }


    public List<Task> getTasksByApprovalStatus(ApprovalStatus approvalStatus) {
        return taskRepository.findAllByApprovalStatusEquals(approvalStatus);
    }


    public List<Task> getTasksByJob(Integer jobId) {
        return taskRepository.findAllByTaskJobEqualsOrderByUpdatedAtDesc(new Job(jobId));
    }


    @Transactional
    public void createNewJob(Job job) {
        jobRepository.save(job);

        if (job.getJobType() == JobType.ADMIN && job.getTasks().size() > 0) {
            job.getTasks().forEach(task -> userService.updatePoints(job.getUser().getId(), task.getPoints()));
        }
    }


    public Job getJobById(Integer jobId) {
        return jobRepository.findJobByIdEquals(jobId);
    }


    public Task getTaskById(Integer taskId) {
        return taskRepository.findByIdEquals(taskId);
    }


    public List<?> getUserOTM(Date startDate, Date endDate) {
        return taskRepository.getUserOfTheMonth(startDate, endDate);
    }


    @Transactional
    public void addTaskToJob(Integer jobId, TaskDto taskDto) {
        // Update "updated_at" column that's in the job table.
        Job jobToUpdate = jobRepository.getOne(jobId);
        jobToUpdate.setUpdatedAt(new Date());
        jobRepository.save(jobToUpdate);

        Task task = modelMapper.map(taskDto, Task.class);
        task.setTaskJob(jobToUpdate);

        switch (jobToUpdate.getJobType()) {
            case SELF:
                task.setApprovalStatus(ApprovalStatus.READY);
                break;
            case ADMIN:
                task.setApprovalStatus(ApprovalStatus.APPROVED);
                userService.updatePoints(jobToUpdate.getUser().getId(), task.getPoints());
                break;
            case EVENT:
                task.setApprovalStatus(ApprovalStatus.WAITING);
        }

        taskRepository.save(task);
    }


    @Transactional
    public Task updateTask(Integer taskId, TaskDto taskDto)
            throws InvocationTargetException, IllegalAccessException {

        Task taskToUpdate = taskRepository.getOne(taskId);
        Map<String, Object> payload = objectMapper.convertValue(taskDto, Map.class);

        if (payload.containsKey("approval_status")) {
            taskToUpdate.setApprovalStatus(Enum.valueOf(ApprovalStatus.class,
                    (String) payload.get("approval_status")));
        }

        BeanUtils.populate(taskToUpdate, payload);
        Task updatedTask = taskRepository.save(taskToUpdate);

        if (payload.containsKey("points")) {
            userService.updatePoints(taskToUpdate.getTaskJob().getUser().getId(), (int) payload.get("points"));
        }

        Job jobToUpdate = taskToUpdate.getTaskJob();
        jobToUpdate.setUpdatedAt(new Date());
        jobRepository.save(jobToUpdate);

        return updatedTask;
    }


    @Transactional
    public void addTaskToAdminJob(Integer userId, TaskDto taskDto) {
        User user = userService.getUserById(userId);
        Job adminJob = jobRepository.findJobByUserEqualsAndJobTypeEquals(user);

        addTaskToJob(adminJob.getId(), taskDto);

        // Update user's self job so it fixes order in the admin page.
        Job jobToUpdate = jobRepository.findJobByUserEqualsAndJobTypeEquals(user, JobType.SELF);
        jobToUpdate.setUpdatedAt(new Date());
        jobRepository.save(jobToUpdate);
    }


    @Transactional
    public void deleteJob(Integer jobId) {
        jobRepository.delete(jobRepository.getOne(jobId));
    }


    @Transactional
    public Task deleteTask(Integer taskId) {
        Task taskToDelete = taskRepository.getOne(taskId);
        Job jobToDelete = jobRepository.getOne(taskToDelete.getTaskJob().getId());
        taskRepository.delete(taskToDelete);

        if (jobToDelete.getTasks().size() == 0 && jobToDelete.getJobType() != JobType.EVENT) {
            jobRepository.delete(jobToDelete);
        }

        return taskToDelete;
    }

}
