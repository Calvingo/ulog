package com.ulog.backend.repository;

import com.ulog.backend.domain.pin.Pin;
import com.ulog.backend.domain.pin.PinSourceType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {

    List<Pin> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Pin> findAllByUserIdAndContactIdOrderByCreatedAtDesc(Long userId, Long contactId);

    List<Pin> findAllByUserIdAndSourceTypeOrderByCreatedAtDesc(Long userId, PinSourceType sourceType);

    List<Pin> findAllByUserIdAndSessionIdOrderByCreatedAtDesc(Long userId, String sessionId);

    Optional<Pin> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndSessionIdAndQaIndex(Long userId, String sessionId, Integer qaIndex);
}

