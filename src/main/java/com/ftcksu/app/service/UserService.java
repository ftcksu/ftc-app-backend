package com.ftcksu.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftcksu.app.model.dto.UserDto;
import com.ftcksu.app.model.entity.*;
import com.ftcksu.app.repository.ImageRepository;
import com.ftcksu.app.repository.JobRepository;
import com.ftcksu.app.repository.MOTDRepository;
import com.ftcksu.app.repository.UserRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final JobRepository jobRepository;

    private final ImageRepository imageRepository;

    private final EventService eventService;

    private final MOTDRepository motdRepository;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    @Autowired
    public UserService(UserRepository userRepository,
                       JobRepository jobRepository,
                       ImageRepository imageRepository,
                       EventService eventService,
                       MOTDRepository motdRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.imageRepository = imageRepository;
        this.eventService = eventService;
        this.motdRepository = motdRepository;
        this.modelMapper = new ModelMapper();
        this.objectMapper = new ObjectMapper();
    }

    private void surpriseStudent(List<User> users) {
        int num = new Random().nextInt((1000));

        if (num < 10) {
            int randomIndex = new Random().nextInt(users.size());
            User secretUser = users.get(randomIndex);
            secretUser.setName("\uD83D\uDC51 " + secretUser.getName());
            secretUser.setPoints(9999);
        }
    }

    public List<User> getAllUsers(boolean includeHidden) {
        if (includeHidden) {
            return userRepository.findAll();
        }

        List<User> users = userRepository.findByHiddenAndRoleNotOrderByUserRankAscNameAsc();
        surpriseStudent(users);

        calculateRanks(users);
        Collections.sort(users, Comparator.comparingInt(User::getUserRank));

        return users;
    }


    public List<String> getUsersDeviceTokens() {
        return userRepository.findUsersDeviceTokens();
    }


    public User getUserById(Integer userId) {
        return userRepository.findUserByIdEquals(userId);
    }


    @Transactional
    public User createNewUser(UserDto userDto) throws EntityExistsException {
        if (userRepository.existsById(userDto.getId())) {
            throw new EntityExistsException("User already exists.");
        }

        User userToAdd = modelMapper.map(userDto, User.class);
        userToAdd.setRole("ROLE_USER");
        User savedUser = userRepository.save(userToAdd);

        Job selfJob = new Job("رصد أعمالي", savedUser, JobType.SELF);
        Job adminJob = new Job("رصد مباشر", savedUser, JobType.ADMIN);
        jobRepository.saveAll(Arrays.asList(selfJob, adminJob));

        updateRanks();
        return savedUser;
    }


    @Transactional
    public User updateUser(Integer userId, UserDto userDto) throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> payload = objectMapper.convertValue(userDto, Map.class);
        return updateUser(userId, payload);
    }

    @Transactional
    public User updateUser(Integer userId, Map<String, Object> payload) throws InvocationTargetException, IllegalAccessException {
        User userToUpdate = userRepository.getOne(userId);

        if (payload.containsKey("device_token")) {
            payload.put("deviceToken", payload.get("device_token"));
            payload.remove("device_token");
        }

        if (payload.containsKey("phone_number")) {
            payload.put("phoneNumber", payload.get("phone_number"));
            payload.remove("phone_number");
        }

        BeanUtils.populate(userToUpdate, payload);
        User updatedUser = userRepository.save(userToUpdate);
        return updatedUser;
    }


    @Transactional
    public User deleteUser(Integer userId) {
        User userToDelete = userRepository.findUserByIdEquals(userId);
        List<Event> userEvents = userToDelete.getEvents();
        List<Job> userJobs = jobRepository.findJobsByUserEqualsOrderByCreatedAtDesc(userToDelete);
        List<MOTD> userMOTDs = motdRepository.findAllByUserEquals(userToDelete);

        userEvents.stream()
                .filter(event -> userToDelete.equals(event.getLeader()))
                .forEach(event -> eventService.deleteEvent(event.getId()));

        userEvents.forEach(event -> event.getUsers().remove(userToDelete));
        jobRepository.deleteAll(userJobs);
        motdRepository.deleteAll(userMOTDs);
        userRepository.delete(userToDelete);

        return userToDelete;
    }


    @Transactional
    public boolean updateProfileImage(Integer userId, Integer imageId) {
        User userToUpdate = userRepository.findUserByIdEquals(userId);
        ProfileImage profileImage = imageRepository.getOne(imageId);

        if (!userToUpdate.equals(profileImage.getUser())) {
            return false;
        }

        ProfileImage userProfileImage = userToUpdate.getProfileImage();

        if (userProfileImage != null) {
            userProfileImage.setUsed(false);
        }

        profileImage.setUsed(true);
        userRepository.save(userToUpdate);
        return true;
    }


    @Transactional
    public User updatePoints(Integer userId, int points) {
        User userToUpdate = userRepository.getOne(userId);
        userToUpdate.adjustPoints(points);
        userToUpdate = userRepository.save(userToUpdate);
        updateRanks();

        return userToUpdate;
    }

    private void updateRanks() {
        userRepository.saveAll(calculateRanks(userRepository.findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByPointsDesc()));
    }

    private List<User> calculateRanks(List<User> users) {
        Collections.sort(users, Comparator.comparingInt(User::getPoints));
        Collections.reverse(users);
        int currentRank = 1, prevCounter = 1, prevPoints = 0;

        for (User user : users) {
            if (user.getPoints() == prevPoints) {
                user.setUserRank(currentRank);
                prevCounter++;
            } else {
                user.setUserRank(prevCounter);
                currentRank = prevCounter++;
            }

            prevPoints = user.getPoints();
        }

        return users;
    }
}
