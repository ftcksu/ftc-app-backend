package com.ftcksu.app.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "taskJob", fetch = FetchType.EAGER)
    @OrderBy("updatedAt DESC")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Task> tasks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private JobType jobType = JobType.EVENT;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnore
    @ToString.Exclude
    private Event event;

    public Job(Integer id) {
        this.id = id;
    }

    public Job(String title, User user, JobType jobType) {
        this.title = title;
        this.user = user;
        this.jobType = jobType;
    }

    public Job(User user, Event event) {
        this.event = event;
        this.title = event.getTitle();
        this.user = user;
    }

    public void setTasks(List<Task> tasks) {
        tasks.forEach(task -> task.setTaskJob(this));
        this.tasks.addAll(tasks);
    }

    public void setDescription(String description) {
        tasks.add(new Task(description, this));
    }

    public boolean getEventStatus() {
        return event != null ? event.isFinished() : false;
    }

    public void setUserId(Integer userId) {
        user = new User(userId);
    }

    public JobUser getUser() {
        return new JobUser(user.getId(), user.getName(), user.getProfileImage());
    }

    public Map<String, Integer> getTasksCount() {
        int waitingCount = 0, readyCount = 0;
        for (Task task : tasks) {
            switch (task.getApprovalStatus()) {
                case WAITING:
                    waitingCount++;
                    break;
                case READY:
                    readyCount++;
            }
        }
        return new HashMap<>(Map.of("WAITING", waitingCount, "READY", readyCount));
    }

    @Data
    public class JobUser {
        private Integer id;
        private String name;
        private ProfileImage profileImage;

        public JobUser(Integer id, String name, ProfileImage profileImage) {
            this.id = id;
            this.name = name;
            this.profileImage = profileImage;
        }
    }
}
