package com.ftcksu.app.service;

import com.ftcksu.app.model.entity.*;
import com.ftcksu.app.repository.ImageRepository;
import com.ftcksu.app.repository.JobRepository;
import com.ftcksu.app.repository.MOTDRepository;
import com.ftcksu.app.repository.UserRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final PasswordEncoder passwordEncoder;

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
        this.passwordEncoder = new BCryptPasswordEncoder();
    }


    public List<User> getAllUsers(boolean includeHidden) {
        if (includeHidden) {
            return userRepository.findAll();
        }

        List<User> users = userRepository.findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByUserRankAscNameAsc();

        int num = new Random().nextInt((1000));

        if (num < 10) {
            int index = users.indexOf(getUserById((int) (Integer.MAX_VALUE / 4.912973177025762)));

            if (index != -1) {
                User secretUser = users.get(index);
                secretUser.setName("\uD83D\uDC51 " + secretUser.getName());
                secretUser.setPoints(9999);
                users = calculateRanks(users);
                Collections.sort(users, Comparator.comparingInt(User::getUserRank));
            }
        }

        return users;
    }


    public List<String> getUsersDeviceTokens() {
        return userRepository.findUsersDeviceTokens();
    }


    public User getUserById(Integer userId) {
        return userRepository.findUserByIdEquals(userId);
    }


    @Transactional
    public void createNewUser(User user) throws EntityExistsException {
        if (userRepository.existsById(user.getId())) {
            throw new EntityExistsException("User already exists.");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setRole("ROLE_USER");
        User savedUser = userRepository.save(user);

        Job selfJob = new Job("رصد أعمالي", savedUser, JobType.SELF);
        Job adminJob = new Job("رصد مباشر", savedUser, JobType.ADMIN);
        jobRepository.saveAll(Arrays.asList(new Job[]{selfJob, adminJob}));

        updateRanks();
    }


    @Transactional
    public void updateUser(Integer userId, Map<String, Object> payload) throws InvocationTargetException, IllegalAccessException {
        // TODO: Add the rest of the banned fields.
        Arrays.asList("points", "userRank").forEach(payload::remove);

        User userToUpdate = userRepository.getOne(userId);
        BeanUtils.populate(userToUpdate, payload);

        if (payload.containsKey("password")) {
            String hashedPassword = new BCryptPasswordEncoder().encode(userToUpdate.getPassword());
            userToUpdate.setPassword(hashedPassword);
        }

        if (payload.containsKey("device_token")) {
            payload.put("deviceToken", payload.get("device_token"));
        }

        if (payload.containsKey("phone_number")) {
            payload.put("phoneNumber", payload.get("phone_number"));
        }

        userRepository.save(userToUpdate);
    }


    @Transactional
    public void deleteUser(Integer userId) {
        User userToDelete = userRepository.findUserByIdEquals(userId);
        List<Event> userEvents = userToDelete.getEvents();
        List<Job> userJobs = jobRepository.findJobsByUserEqualsOrderByUpdatedAtDesc(userToDelete);
        List<MOTD> userMOTDs = motdRepository.findAllByUserEquals(userToDelete);

        userEvents.stream()
                .filter(event -> userToDelete.equals(event.getLeader()))
                .forEach(event -> eventService.deleteEvent(event.getId()));

        userEvents.forEach(event -> event.getUsers().remove(userToDelete));
        jobRepository.deleteAll(userJobs);
        motdRepository.deleteAll(userMOTDs);
        userRepository.delete(userToDelete);
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
    public void updatePoints(Integer userId, int points) {
        User userToUpdate = userRepository.getOne(userId);
        userToUpdate.adjustPoints(points);
        userRepository.save(userToUpdate);
        updateRanks();
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
