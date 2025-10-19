package com.ulog.backend.goal.service;

import com.ulog.backend.domain.goal.ActionPlan;
import com.ulog.backend.domain.goal.Reminder;
import com.ulog.backend.domain.goal.enums.ReminderStatus;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.goal.dto.ReminderResponse;
import com.ulog.backend.repository.ReminderRepository;
import com.ulog.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    @Value("${reminder.advance-minutes:15}")
    private int advanceMinutes;

    public ReminderService(ReminderRepository reminderRepository, 
                          UserRepository userRepository) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createRemindersForActionPlan(ActionPlan actionPlan) {
        // 只为已采纳的行动计划创建提醒
        if (!Boolean.TRUE.equals(actionPlan.getIsAdopted())) {
            log.debug("Skipping reminder creation for non-adopted action plan {}", actionPlan.getId());
            return;
        }

        // 提前advanceMinutes分钟提醒
        LocalDateTime remindTime = actionPlan.getScheduledTime().minusMinutes(advanceMinutes);
        
        // 如果提醒时间已经过了，就设置为当前时间
        if (remindTime.isBefore(LocalDateTime.now())) {
            remindTime = LocalDateTime.now();
        }

        Reminder reminder = new Reminder(actionPlan, actionPlan.getGoal().getUser(), remindTime);
        reminderRepository.save(reminder);
        
        log.info("Created reminder {} for action plan {} at {}", 
                 reminder.getId(), actionPlan.getId(), remindTime);
    }

    @Transactional
    public void cancelRemindersForActionPlan(ActionPlan actionPlan) {
        List<Reminder> reminders = reminderRepository.findAllByActionPlan(actionPlan);
        
        for (Reminder reminder : reminders) {
            if (reminder.getStatus() == ReminderStatus.PENDING) {
                reminder.setStatus(ReminderStatus.CANCELLED);
                reminderRepository.save(reminder);
                log.info("Cancelled reminder {} for action plan {}", reminder.getId(), actionPlan.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getUpcomingReminders(Long userId) {
        User user = userRepository.findActiveById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Reminder> reminders = reminderRepository.findUpcomingRemindersByUser(
            user, LocalDateTime.now(), ReminderStatus.PENDING);

        return reminders.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Reminder> getPendingReminders() {
        return reminderRepository.findPendingRemindersBeforeTime(
            LocalDateTime.now(), ReminderStatus.PENDING);
    }

    @Transactional
    public void markAsSent(Reminder reminder) {
        reminder.setStatus(ReminderStatus.SENT);
        reminder.setSentAt(LocalDateTime.now());
        reminderRepository.save(reminder);
    }

    @Transactional
    public void markAsFailed(Reminder reminder) {
        reminder.setStatus(ReminderStatus.FAILED);
        reminderRepository.save(reminder);
    }

    private ReminderResponse mapToResponse(Reminder reminder) {
        ActionPlan plan = reminder.getActionPlan();
        return new ReminderResponse(
            reminder.getId(),
            plan.getId(),
            plan.getTitle(),
            plan.getDescription(),
            plan.getGoal().getId(),
            plan.getGoal().getGoalDescription(),
            plan.getGoal().getContact().getId(),
            plan.getGoal().getContact().getName(),
            reminder.getRemindTime(),
            reminder.getStatus(),
            reminder.getSentAt(),
            reminder.getCreatedAt()
        );
    }
}

