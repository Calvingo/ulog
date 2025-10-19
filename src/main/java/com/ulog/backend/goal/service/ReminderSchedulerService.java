package com.ulog.backend.goal.service;

import com.ulog.backend.domain.goal.Reminder;
import com.ulog.backend.push.PushNotificationService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReminderSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerService.class);

    private final ReminderService reminderService;
    private final PushNotificationService pushNotificationService;

    public ReminderSchedulerService(ReminderService reminderService,
                                   PushNotificationService pushNotificationService) {
        this.reminderService = reminderService;
        this.pushNotificationService = pushNotificationService;
    }

    @Scheduled(cron = "${reminder.scheduler.cron:0 * * * * *}")
    public void sendPendingReminders() {
        log.debug("Checking for pending reminders...");

        try {
            List<Reminder> pendingReminders = reminderService.getPendingReminders();
            
            if (pendingReminders.isEmpty()) {
                log.debug("No pending reminders found");
                return;
            }

            log.info("Found {} pending reminders to send", pendingReminders.size());

            for (Reminder reminder : pendingReminders) {
                try {
                    sendReminder(reminder);
                    reminderService.markAsSent(reminder);
                    log.info("Successfully sent reminder {}", reminder.getId());
                } catch (Exception e) {
                    log.error("Failed to send reminder {}: {}", reminder.getId(), e.getMessage());
                    reminderService.markAsFailed(reminder);
                }
            }
        } catch (Exception e) {
            log.error("Error in reminder scheduler: {}", e.getMessage(), e);
        }
    }

    private void sendReminder(Reminder reminder) {
        String title = "行动计划提醒";
        String body = buildReminderBody(reminder);
        
        pushNotificationService.sendToUser(
            reminder.getUser(),
            title,
            body
        );
    }

    private String buildReminderBody(Reminder reminder) {
        String contactName = reminder.getActionPlan().getGoal().getContact().getName();
        String actionTitle = reminder.getActionPlan().getTitle();
        
        return String.format("关于 %s 的行动计划「%s」即将开始", contactName, actionTitle);
    }
}

