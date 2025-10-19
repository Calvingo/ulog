package com.ulog.backend.repository;

import com.ulog.backend.domain.goal.UserPushToken;
import com.ulog.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPushTokenRepository extends JpaRepository<UserPushToken, Long> {

    List<UserPushToken> findAllByUserAndIsActiveTrue(User user);

    Optional<UserPushToken> findByDeviceToken(String deviceToken);
}

