package com.ftcksu.app.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String description;

    private Integer points;

    private ApprovalStatus approvalStatus = ApprovalStatus.READY;

    @ManyToOne(optional = false, targetEntity = Job.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Job taskJob;

    public Task(String description, Job taskJob) {
        this.description = description;
        this.taskJob = taskJob;
    }
}
