package com.ulog.backend.repository;

import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.goal.RelationshipGoal;
import com.ulog.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationshipGoalRepository extends JpaRepository<RelationshipGoal, Long> {

    List<RelationshipGoal> findAllByUserAndDeletedFalseOrderByCreatedAtDesc(User user);

    List<RelationshipGoal> findAllByUserAndContactAndDeletedFalseOrderByCreatedAtDesc(User user, Contact contact);

    Optional<RelationshipGoal> findByIdAndUserAndDeletedFalse(Long id, User user);
}

