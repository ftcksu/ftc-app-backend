package com.ftcksu.app.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProfileImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fileName;

    @JsonIgnore
    private String imageName;

    @JsonIgnore
    private String thumbName;

    private boolean used;

    private ApprovalStatus approved = ApprovalStatus.WAITING;

    @ManyToOne(optional = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private User user;

    public ProfileImage(String fileName, String imageName, String thumbName, User user) {
        this.fileName = fileName;
        this.imageName = imageName;
        this.thumbName = thumbName;
        this.user = user;
    }

    public String getUserName() {
        return user.getName();
    }

    public Integer getUserId() {
        return user.getId();
    }
}
