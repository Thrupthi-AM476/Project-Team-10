package com.vps.repository;

import com.vps.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // "Find the user with this email" (used by login)
    Optional<User> findByEmail(String email);

    // "Does anyone already have this email?" (used by registration)
    boolean existsByEmail(String email);
}
