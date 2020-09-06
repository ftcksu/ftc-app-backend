package com.ftcksu.app.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    @JsonIgnore
    List<ProfileImage> imageHistory = new LinkedList<>();
    @Id
    private Integer id;
    private String name;
    private String phoneNumber;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String bio;
    private String role;
    private int points;
    private int userRank;
    private boolean hidden;
    private String deviceToken;
    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    @JsonIgnore
    @ToString.Exclude
    private List<Event> events = new ArrayList<>();

    public User(Integer id) {
        this.id = id;
    }

    public User(Integer id, Event event) {
        this.id = id;
        events.add(event);
    }

    public void adjustPoints(int points) {
        this.points += points;
    }

    public void setPassword(String password) {
       this.password = encoder.encode(password);
    }

    public ProfileImage getProfileImage() {
        for (ProfileImage profileImage : imageHistory) {
            if (profileImage.isUsed()) {
                return profileImage;
            }
        }
        return null;
    }
}
