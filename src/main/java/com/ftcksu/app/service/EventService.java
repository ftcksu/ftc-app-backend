package com.ftcksu.app.service;

import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.repository.EventRepository;
import com.ftcksu.app.repository.JobRepository;
import com.ftcksu.app.repository.UserRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EventService {

    private final EventRepository eventRepository;

    private final JobRepository jobRepository;

    private final UserRepository userRepository;

    @Autowired
    public EventService(EventRepository eventRepository, JobRepository jobRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }


    public List<Event> getAllEvents() {
        return eventRepository.findEventsByOrderByCreatedAtDesc();
    }


    public List<Event> getEventsByUser(Integer userId, boolean leader) {
        return leader ? eventRepository.findEventsByLeaderEqualsOrderByCreatedAtDesc(new User(userId)) :
                eventRepository.findEventsByUsersContainingOrderByCreatedAtDesc(new User(userId));
    }


    public Set<User> getUsersByEvent(Integer eventId) {
        return eventRepository.getOne(eventId).getUsers();
    }


    public List<Job> getJobsByEvent(Event event) {
        return jobRepository.findJobsByEventEqualsOrderByCreatedAtAsc(event);
    }


    public List<String> getUsersDeviceTokensByEvent(Integer eventId) {
        return eventRepository.findUsersDeviceTokensByEvent(eventId);
    }


    public Event getEventById(Integer id) {
        return eventRepository.findEventByIdEquals(id);
    }


    @Transactional
    public Event createNewEvent(Event event) {
        List<User> usersToAdd = new ArrayList<>(event.getUsers());
        event.getUsers().clear();

        // This edits the user table in the database, so we need to fetch the whole user or it'll changes all the user's
        // fields to null.
        Event savedEvent = eventRepository.save(event);

        usersToAdd.add(0, event.getLeader());
        usersToAdd.forEach(user -> addUserToEvent(savedEvent.getId(), user.getId()));

        return getEventById(savedEvent.getId());
    }


    @Transactional
    public boolean addUserToEvent(Integer eventId, Integer userId) {
        if (!(userRepository.existsById(userId) && eventRepository.existsById(eventId))) {
            return false;
        }

        User userToAdd = userRepository.findUserByIdEquals(userId);
        Event eventToUpdate = eventRepository.findEventByIdEquals(eventId);
        Set<User> eventUsers = eventToUpdate.getUsers();

        if (eventUsers.size() >= eventToUpdate.getMaxUsers() || eventUsers.contains(userToAdd)) {
            return false;
        }

        eventUsers.add(userToAdd);
        Event savedEvent = eventRepository.save(eventToUpdate);

        // Add a new Job to the user if it doesn't exist.
        if (!jobRepository.existsJobByUserEqualsAndEventEquals(userToAdd, savedEvent)) {
            Job jobToInsert = new Job(userToAdd, savedEvent);
            jobRepository.save(jobToInsert);
        }

        return true;
    }


    @Transactional
    public void updateEvent(Integer eventId, Map<String, Object> payload)
            throws InvocationTargetException, IllegalAccessException, ParseException {
        Arrays.asList("leader", "users").forEach(payload::remove);


        Event eventToUpdate = eventRepository.findEventByIdEquals(eventId);

        if (payload.containsKey("date")) {
            payload.replace("date", DateUtils
                    .addHours(new SimpleDateFormat("yyyy-MM-dd")
                            .parse((String) payload.get("date")), 12));
        }

        if (payload.containsKey("max_users")) {
            payload.put("maxUsers", payload.get("max_users"));
        }

        if (payload.containsKey("whats_app_link")) {
            payload.put("whatsAppLink", payload.get("whats_app_link"));
        }

        BeanUtils.populate(eventToUpdate, payload);
        eventRepository.save(eventToUpdate);
    }


    @Transactional
    public void deleteEvent(Integer eventId) {
        Event eventToDelete = eventRepository.findEventByIdEquals(eventId);
        List<Job> eventJobs = jobRepository.findJobsByEventEqualsOrderByCreatedAtAsc(eventToDelete);

        eventJobs.forEach(job -> job.setEvent(null));
        jobRepository.saveAll(eventJobs);

        eventRepository.delete(eventToDelete);
    }


    @Transactional
    public void removeUser(Integer eventId, Integer userId) {
        Event eventToUpdate = eventRepository.getOne(eventId);
        User userToRemove = userRepository.findUserByIdEquals(userId);

        if (userToRemove.equals(eventToUpdate.getLeader())) {
            deleteEvent(eventId);
        } else {
            eventToUpdate.getUsers().remove(userToRemove);
            eventRepository.save(eventToUpdate);
        }
    }

}
