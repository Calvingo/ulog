package com.ulog.backend.repository;

import com.ulog.backend.domain.compliance.UserPrivacyConsent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrivacyConsentRepository extends JpaRepository<UserPrivacyConsent, Long> {

    List<UserPrivacyConsent> findByUserIdOrderByConsentTimeDesc(Long userId);

    @Query("SELECT upc FROM UserPrivacyConsent upc WHERE upc.userId = :userId " +
           "AND upc.policyVersion = :version ORDER BY upc.consentTime DESC")
    Optional<UserPrivacyConsent> findLatestByUserIdAndVersion(@Param("userId") Long userId, 
                                                               @Param("version") String version);

    @Query("SELECT upc FROM UserPrivacyConsent upc WHERE upc.userId = :userId " +
           "ORDER BY upc.consentTime DESC LIMIT 1")
    Optional<UserPrivacyConsent> findLatestByUserId(@Param("userId") Long userId);
}

