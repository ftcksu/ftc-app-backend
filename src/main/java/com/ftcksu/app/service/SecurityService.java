package com.ftcksu.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityService {
    private final EventService eventService;
    private final JobService jobService;

    @Autowired
    public SecurityService(EventService eventService,
                           JobService jobService) {
        this.eventService = eventService;
        this.jobService = jobService;
    }

    public Integer getLoggedUserId() {
        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userDetails instanceof UserDetails) {
            return Integer.parseInt(((UserDetails) userDetails).getUsername());
        }

        return null;
    }

    @Transactional
    public boolean isLoggedUser(Integer userId) {
        return getLoggedUserId().equals(userId);
    }

    @Transactional
    public boolean isEventLeader(Integer eventId) {
        return getLoggedUserId().equals(eventService.getEventLeader(eventId));
    }

    @Transactional
    public boolean isJobOwner(Integer jobId) {
        return getLoggedUserId().equals(jobService.getJobOwner(jobId));
    }

    @Transactional
    public boolean isEventLeaderSeeingTasks(Integer jobId) {
        return getLoggedUserId().equals(jobService.getEventLeaderByJob(jobId));
    }

    @Transactional
    public boolean isTaskOwner(Integer taskId) {
        return getLoggedUserId().equals(jobService.getTaskOwner(taskId));
    }
}

