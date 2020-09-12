package com.ftcksu.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String title;

    @NotNull
    private String description;

    private Date date;

    private String whatsAppLink;

    private Integer maxUsers;

    private String location;

    private boolean finished;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leader_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User leader;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "event_users", joinColumns = @JoinColumn(name = "event_id", updatable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", updatable = false))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<User> users = new HashSet<>();

    public Event(Integer eventId) {
        this.id = eventId;
    }

    public void setUsers(List<User> users) {
        this.users.addAll(users);
    }

    public void setLeaderId(Integer id){this.leader = new User(id);}

    public boolean isFull() {
        return users.size() >= maxUsers;
    }

}
