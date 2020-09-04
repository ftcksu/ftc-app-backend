package com.ftcksu.app.repository;

import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ProfileImage, Integer> {

    List<ProfileImage> findProfileImagesByApprovedEquals(ApprovalStatus approvalStatus);

    ProfileImage findProfileImageByIdEquals(Integer id);

}
