package com.ftcksu.app.repository;

import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findAllByApprovalStatusEquals(ApprovalStatus approvalStatus);

    List<Task> findAllByTaskJobEqualsOrderByUpdatedAtDesc(Job job);

    Task findByIdEquals(Integer id);

    @Query("SELECT new map(u.id AS user_id, u.name AS name, SUM(t.points) AS points) " +
            "FROM Task t INNER JOIN t.taskJob j INNER JOIN j.user u " +
            "WHERE t.createdAt >= ?1 AND t.createdAt < ?2 " +
            "GROUP BY u.id " +
            "ORDER BY SUM(t.points) DESC")
    List<?> getUserOfTheMonth(Date startDate, Date endDate);

    @Query("SELECT t.taskJob.user.id FROM Task t WHERE t.id = :id")
    Integer findTaskOwner(Integer id);

}
