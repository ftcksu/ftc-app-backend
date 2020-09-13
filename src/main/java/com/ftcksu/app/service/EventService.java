package com.ftcksu.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftcksu.app.model.dto.EventDto;
import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.repository.EventRepository;
import com.ftcksu.app.repository.JobRepository;
import com.ftcksu.app.repository.UserRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EventService {

    private final EventRepository eventRepository;

    private final JobRepository jobRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    @Autowired
    public EventService(EventRepository eventRepository, JobRepository jobRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.modelMapper = new ModelMapper();
        this.objectMapper = new ObjectMapper();
    }

    public List<Event> getAllEvents() {
        return eventRepository.findEventsByOrderByCreatedAtDesc();
    }

    public List<Event> getEventsByUser(Integer userId, boolean leader) {
        return leader ? eventRepository.findEventsByLeaderEqualsOrderByCreatedAtDesc(new User(userId)) :
                eventRepository.findEventsByUsersContainingOrderByCreatedAtDesc(new User(userId));
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

    public Integer getEventLeader(Integer id) {
        return eventRepository.findEventLeader(id);
    }

    @Transactional
    public Event createNewEvent(EventDto eventDto) {
        Event eventToCreate = modelMapper.map(eventDto, Event.class);

        Event savedEvent = eventRepository.save(eventToCreate);
        addUserToEvent(savedEvent.getId(), savedEvent.getLeader().getId());

        return savedEvent;
    }

    @Transactional
    public User addUserToEvent(Integer eventId, Integer userId) {

        User userToAdd = userRepository.findUserByIdEquals(userId);
        Event eventToUpdate = eventRepository.findEventByIdEquals(eventId);
        Set<User> eventUsers = eventToUpdate.getUsers();

        if (eventUsers.size() >= eventToUpdate.getMaxUsers() || eventUsers.contains(userToAdd)) {
            throw new EntityExistsException("could not add the user due to the max size or the user already exist.");
        }

        eventUsers.add(userToAdd);
        Event savedEvent = eventRepository.save(eventToUpdate);

        // Add a new Job to the user if it doesn't exist.
        if (!jobRepository.existsJobByUserEqualsAndEventEquals(userToAdd, savedEvent)) {
            Job jobToInsert = new Job(userToAdd, savedEvent);
            jobRepository.save(jobToInsert);
        }

        return userToAdd;
    }

    @Transactional
    public Event updateEvent(Integer eventId, EventDto eventDto)
            throws InvocationTargetException, IllegalAccessException {

        Event eventToUpdate = eventRepository.findEventByIdEquals(eventId);
        Map<String, Object> payload = objectMapper.convertValue(eventDto, Map.class);

        BeanUtils.populate(eventToUpdate, payload);
        Event updatedEvent = eventRepository.save(eventToUpdate);

        return updatedEvent;
    }

    @Transactional
    public Event deleteEvent(Integer eventId) {
        Event eventToDelete = eventRepository.findEventByIdEquals(eventId);
        List<Job> eventJobs = jobRepository.findJobsByEventEqualsOrderByCreatedAtAsc(eventToDelete);

        eventJobs.forEach(job -> job.setEvent(null));
        jobRepository.saveAll(eventJobs);
        eventRepository.delete(eventToDelete);

        return eventToDelete;
    }

    @Transactional
    public User removeUser(Integer eventId, Integer userId) {
        Event eventToUpdate = eventRepository.getOne(eventId);
        User userToRemove = userRepository.findUserByIdEquals(userId);

        if (userToRemove.equals(eventToUpdate.getLeader())) {
            deleteEvent(eventId);
        } else {
            eventToUpdate.getUsers().remove(userToRemove);
            eventRepository.save(eventToUpdate);
        }

        return userToRemove;
    }
}
