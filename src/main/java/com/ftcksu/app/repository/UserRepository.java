package com.ftcksu.app.repository;

import com.ftcksu.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findUserByIdEquals(Integer id);

    @Query("SELECT u.deviceToken FROM User u WHERE u.hidden = false AND u.deviceToken IS NOT NULL")
    List<String> findUsersDeviceTokens();

    default List<User> findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByPointsDesc() {
        return findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByPointsDesc("ROLE_ADMIN");
    }

    List<User> findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByPointsDesc(String role);

    default List<User> findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByUserRankAscNameAsc() {
        return findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByUserRankAscNameAsc("ROLE_ADMIN");
    }

    List<User> findAllByHiddenIsFalseAndRoleNotIgnoreCaseOrderByUserRankAscNameAsc(String role);

}
