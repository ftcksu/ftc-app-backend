package com.ftcksu.app.service;

import com.ftcksu.app.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityService {
    private final UserDetailsService userDetailsService;
    private final EventService eventService;
    private final JobService jobService;
    private final JWTUtil jwt;

    @Autowired
    public SecurityService(UserDetailsService userDetailsService,
                           EventService eventService,
                           JobService jobService,
                           JWTUtil jwt) {
        this.userDetailsService = userDetailsService;
        this.eventService = eventService;
        this.jobService = jobService;
        this.jwt = jwt;
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
    public boolean isTaskOwner(Integer taskId) {
        return getLoggedUserId().equals(jobService.getTaskOwner(taskId));
    }
}

