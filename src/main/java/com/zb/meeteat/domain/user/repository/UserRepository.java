package com.zb.meeteat.domain.user.repository;

import com.zb.meeteat.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
