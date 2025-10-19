package com.ulog.backend.repository;

import com.ulog.backend.domain.goal.ActionPlan;
import com.ulog.backend.domain.goal.RelationshipGoal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {

    List<ActionPlan> findAllByGoalAndDeletedFalseOrderByOrderIndexAsc(RelationshipGoal goal);

    Optional<ActionPlan> findByIdAndDeletedFalse(Long id);
}

