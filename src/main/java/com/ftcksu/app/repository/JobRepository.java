package com.ftcksu.app.repository;

import com.ftcksu.app.model.entity.Event;
import com.ftcksu.app.model.entity.Job;
import com.ftcksu.app.model.entity.JobType;
import com.ftcksu.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {

    List<Job> findJobsByUserEqualsOrderByCreatedAtDesc(User user);

    List<Job> findJobsByEventEqualsOrderByCreatedAtAsc(Event event);

    @Query("SELECT j FROM Job j WHERE j.jobType = :jobType AND j.user.hidden = false ORDER BY j.updatedAt DESC")
    List<Job> findJobsByJobType(JobType jobType);

    @Query("SELECT j.user.id FROM Job j WHERE j.id = :id")
    Integer findJobOwner(Integer id);

    @Query("SELECT j.event.leader.id FROM Job j  WHERE j.id = :id")
    Integer findEventLeaderByJob(Integer id);

    Job findJobByIdEquals(Integer id);

    default Job findFirstByUserEqualsAndJobTypeEquals(User user) {
        return findFirstByUserEqualsAndJobTypeEquals(user, JobType.ADMIN);
    }

    Job findFirstByUserEqualsAndJobTypeEquals(User user, JobType jobType);

    boolean existsJobByUserEqualsAndEventEquals(User user, Event Event);

}
