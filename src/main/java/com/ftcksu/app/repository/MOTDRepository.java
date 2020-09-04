package com.ftcksu.app.repository;

import com.ftcksu.app.model.entity.MOTD;
import com.ftcksu.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MOTDRepository extends JpaRepository<MOTD, Integer> {

    MOTD findFirstByOrderByIdDesc();

    List<MOTD> findAllByUserEquals(User user);

}
