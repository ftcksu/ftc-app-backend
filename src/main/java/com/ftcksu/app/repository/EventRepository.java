package com.ftcksu.app.repository;

import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findEventsByOrderByCreatedAtDesc();

    List<Event> findEventsByUsersContainingOrderByCreatedAtDesc(User user);

    List<Event> findEventsByLeaderEqualsOrderByCreatedAtDesc(User user);

    @Query("SELECT u.deviceToken FROM Event e INNER JOIN e.users u WHERE e.id = :id AND u.deviceToken IS NOT NULL")
    List<String> findUsersDeviceTokensByEvent(Integer id);

    Event findEventByIdEquals(Integer id);

}
