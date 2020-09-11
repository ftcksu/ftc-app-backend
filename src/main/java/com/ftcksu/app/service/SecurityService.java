package com.ftcksu.app.service;

import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.Task;
import com.ftcksu.app.model.request.AuthenticationRequest;
import com.ftcksu.app.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private AuthenticationManager authenticationManager;

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

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
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
        // TODO: Replace with a getEventLeader method.
        Event event = eventService.getEventById(eventId);
        return event != null ? getLoggedUserId().equals(event.getLeader().getId()) : false;
    }

    @Transactional
    public boolean isJobOwner(Integer jobId) {
        // TODO: Replace with a getJobOwner method.
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            return false;
        }

        Event jobEvent = job.getEvent();
        boolean eventLeader = false;
        if (jobEvent != null) {
            // TODO: Replace with a getEventLeader method.
            eventLeader = getLoggedUserId().equals(jobEvent.getLeader().getId());
        }

        return eventLeader || getLoggedUserId().equals(job.getUser().getId());
    }

    @Transactional
    public boolean isTaskOwner(Integer taskId) {
        // TODO: Replace with a getJobOwner method.
        Task task = jobService.getTaskById(taskId);
        return task != null ? getLoggedUserId().equals(task.getTaskJob().getUser().getId()) : false;
    }

    @Transactional
    public String login(AuthenticationRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
                request.getPassword(), userDetails.getAuthorities());

        authenticationManager.authenticate(authenticationToken);

        if (authenticationToken.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        return jwt.generateToken(userDetails);
    }
}

