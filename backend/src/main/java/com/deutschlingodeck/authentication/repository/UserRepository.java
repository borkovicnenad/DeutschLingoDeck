package com.deutschlingodeck.authentication.repository;

import com.deutschlingodeck.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
