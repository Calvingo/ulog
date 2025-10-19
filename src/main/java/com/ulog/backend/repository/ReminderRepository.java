package com.ulog.backend.repository;

import com.ulog.backend.domain.goal.ActionPlan;
import com.ulog.backend.domain.goal.Reminder;
import com.ulog.backend.domain.goal.enums.ReminderStatus;
import com.ulog.backend.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    @Query("SELECT r FROM Reminder r " +
           "JOIN FETCH r.actionPlan ap " +
           "JOIN FETCH ap.goal g " +
           "JOIN FETCH g.contact c " +
           "JOIN FETCH r.user u " +
           "WHERE r.remindTime <= :now AND r.status = :status")
    List<Reminder> findPendingRemindersBeforeTime(@Param("now") LocalDateTime now, 
                                                    @Param("status") ReminderStatus status);

    @Query("SELECT r FROM Reminder r " +
           "JOIN FETCH r.actionPlan ap " +
           "JOIN FETCH ap.goal g " +
           "JOIN FETCH g.contact c " +
           "WHERE r.user = :user AND r.remindTime > :now " +
           "AND r.status = :status ORDER BY r.remindTime ASC")
    List<Reminder> findUpcomingRemindersByUser(@Param("user") User user, 
                                                 @Param("now") LocalDateTime now,
                                                 @Param("status") ReminderStatus status);

    List<Reminder> findAllByActionPlan(ActionPlan actionPlan);
}

