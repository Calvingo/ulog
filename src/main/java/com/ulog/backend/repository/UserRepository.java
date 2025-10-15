package com.ulog.backend.repository;

import com.ulog.backend.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false AND u.status = 1")
    Optional<User> findActiveById(@Param("id") Long id);
}
