package com.ftcksu.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftcksu.app.model.dto.JobDto;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JobService {

    private final JobRepository jobRepository;

    private final TaskRepository taskRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    @Autowired
    public JobService(JobRepository jobRepository, TaskRepository taskRepository, UserService userService) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.modelMapper = new ModelMapper();
        this.objectMapper = new ObjectMapper();
    }


    public List<Job> getJobsByUser(User user) {
        return jobRepository.findJobsByUserEqualsOrderByCreatedAtDesc(user);
    }

    public Integer getJobOwner(Integer id) {
        return jobRepository.findJobOwner(id);
    }

    public Integer getTaskOwner(Integer id) {
        return taskRepository.findTaskOwner(id);
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
    public Job createNewJob(JobDto jobDto) {
        Job jobToCreate = modelMapper.map(jobDto, Job.class);
        Job savedJob = jobRepository.save(jobToCreate);

        if (jobToCreate.getJobType() == JobType.ADMIN && jobToCreate.getTasks().size() > 0) {
            jobToCreate.getTasks().forEach(task -> userService.updatePoints(jobToCreate.getUser().getId(), task.getPoints()));
        }
        return savedJob;
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
    public Task addTaskToJob(Integer jobId, TaskDto taskDto) {
        // Update "updated_at" column that's in the job table.
        Job jobToUpdate = jobRepository.getOne(jobId);
        jobToUpdate.setUpdatedAt(new Date());
        jobToUpdate = jobRepository.save(jobToUpdate);

        Task task = modelMapper.map(taskDto, Task.class);
        task.setTaskJob(jobToUpdate);

        switch (jobToUpdate.getJobType()) {
            case SELF:
                task.setApprovalStatus(ApprovalStatus.READY);
                break;
            case EVENT:
                task.setApprovalStatus(ApprovalStatus.WAITING);
                break;
            case ADMIN:
                task.setApprovalStatus(ApprovalStatus.APPROVED);
        }

        Task addedTask = taskRepository.save(task);
        return addedTask;
    }


    @Transactional
    public Task updateTask(Integer taskId, TaskDto taskDto)
            throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> payload = objectMapper.convertValue(taskDto, Map.class);
        return updateTask(taskId, payload);
    }

    @Transactional
    public Task updateTask(Integer taskId, Map<String, Object> payload) throws InvocationTargetException, IllegalAccessException {
        Task taskToUpdate = taskRepository.getOne(taskId);

        Job jobToUpdate = taskToUpdate.getTaskJob();
        jobToUpdate.setUpdatedAt(new Date());
        jobRepository.save(jobToUpdate);

        if (payload.containsKey("approval_status")) {
            taskToUpdate.setApprovalStatus(Enum.valueOf(ApprovalStatus.class,
                    (String) payload.get("approval_status")));
            payload.remove("approval_status");
        }

        if (payload.containsKey("approvalStatus")) {
            taskToUpdate.setApprovalStatus(Enum.valueOf(ApprovalStatus.class,
                    (String) payload.get("approvalStatus")));
            payload.remove("approvalStatus");
        }

        BeanUtils.populate(taskToUpdate, payload);
        Task updatedTask = taskRepository.save(taskToUpdate);

        if (payload.containsKey("points")) {
            userService.updatePoints(taskToUpdate.getTaskJob().getUser().getId(), (int) payload.get("points"));
        }

        return updatedTask;
    }

    @Transactional
    public Task addTaskToAdminJob(Integer userId, Task task) {
        User user = userService.updatePoints(userId, task.getPoints());
        Job adminJob = jobRepository.findFirstByUserEqualsAndJobTypeEquals(user);
        adminJob.setUpdatedAt(new Date());
        jobRepository.save(adminJob);

        task.setApprovalStatus(ApprovalStatus.APPROVED);
        task.setTaskJob(adminJob);
        Task addedTask = taskRepository.save(task);

        // Update user's self job so it fixes order in the admin page.
        Job jobToUpdate = jobRepository.findFirstByUserEqualsAndJobTypeEquals(user, JobType.SELF);
        jobToUpdate.setUpdatedAt(new Date());
        jobRepository.save(jobToUpdate);

        return addedTask;
    }


    @Transactional
    public Job deleteJob(Integer jobId) {
        Job jobDelete = jobRepository.getOne(jobId);
        jobRepository.delete(jobDelete);
        return jobDelete;
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
