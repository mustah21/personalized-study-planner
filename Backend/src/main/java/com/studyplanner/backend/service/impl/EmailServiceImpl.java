package com.studyplanner.backend.service.impl;

import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
    private static final String UTF = "UTF-8";


    // ---- Email Service Implementation for Sending Task Reminders ----

    @Override
    @Async // sends email in a background thread so it doesn't block the scheduler
    public void sendTaskReminderEmail(String toEmail, String firstName, Task task) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF);

            helper.setTo(toEmail);
            helper.setSubject("Reminder: \"" + task.getTaskName() + "\" is due tomorrow!");
            helper.setText(buildReminderEmailBody(firstName, task), true);
            mailSender.send(message);
            log.info("Reminder email sent to {} for task '{}'", toEmail, task.getTaskName());
        } catch (MessagingException e) {
            log.error("Failed to send reminder email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ---- Email Service Implementation for Sending Welcome Emails ----
    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Our Personalized Study Planner");
            helper.setText(buildWelcomeEmailBody(firstName), true);
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }
    // Share Invite Email — Accept / Decline buttons included

    @Override
    @Async
    public void sendShareInviteEmail(String toEmail,
                                     String recipientFirstName,
                                     User sender,
                                     Task task,
                                     String acceptUrl,
                                     String declineUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF);
            helper.setTo(toEmail);
            helper.setSubject(sender.getFirstName() + " " + sender.getLastName()
                    + " shared a task with you on Personalized Study Planner");
            helper.setText(buildShareInviteEmailBody(
                    recipientFirstName, sender, task, acceptUrl, declineUrl), true);
            mailSender.send(message);
            log.info("Share invite email sent to {} for task '{}'", toEmail, task.getTaskName());
        } catch (MessagingException e) {
            log.error("Failed to send share invite email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Email Body Builders
    // ─────────────────────────────────────────────────────────────────────────────

    private String buildShareInviteEmailBody(String recipientFirstName,
                                             User sender,
                                             Task task,
                                             String acceptUrl,
                                             String declineUrl) {
        String deadline = task.getTaskDeadline() != null
                ? task.getTaskDeadline().format(FORMATTER)
                : "No deadline set";

        // Avatar: profile picture if available, else initials fallback
        String senderAvatar = sender.getProfilePicture() != null
                ? "<img src='" + sender.getProfilePicture() + "' style='width:48px;height:48px;" +
                "border-radius:50%;object-fit:cover;vertical-align:middle;margin-right:12px;'/>"
                : "<span style='display:inline-block;width:48px;height:48px;border-radius:50%;" +
                "background:#2c3e50;color:#fff;text-align:center;line-height:48px;font-size:20px;" +
                "font-weight:bold;vertical-align:middle;margin-right:12px;'>" +
                sender.getFirstName().charAt(0) + "</span>";

        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                <style>
                  body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
                  .container{max-width:600px;margin:30px auto;background:#fff;
                             border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
                  .header{background:#2c3e50;padding:24px 32px;color:#fff}
                  .header h1{margin:0;font-size:22px}
                  .header p{margin:6px 0 0;font-size:14px;color:#bdc3c7}
                  .body{padding:32px}
                  .sender-card{display:flex;align-items:center;background:#f8f9fa;
                               border-radius:8px;padding:16px 20px;margin-bottom:24px}
                  .sender-name{font-weight:bold;font-size:16px;color:#2c3e50}
                  .sender-email{font-size:13px;color:#7f8c8d;margin-top:3px}
                  .task-card{background:#f8f9fa;border-left:4px solid #2c3e50;
                             border-radius:4px;padding:20px 24px;margin-bottom:24px}
                  .task-title{font-size:18px;font-weight:bold;color:#2c3e50;margin:0 0 14px}
                  .task-row{font-size:14px;color:#555;margin-bottom:10px}
                  .task-label{font-weight:600;color:#7f8c8d;margin-right:6px}
                  .actions{text-align:center;margin:28px 0}
                  .btn{display:inline-block;padding:14px 36px;border-radius:6px;
                       font-size:15px;font-weight:bold;text-decoration:none;margin:0 8px}
                  .btn-accept{background:#27ae60;color:#fff}
                  .btn-decline{background:#e74c3c;color:#fff}
                  .note{font-size:12px;color:#95a5a6;text-align:center;margin-bottom:20px}
                  .footer{background:#f8f9fa;padding:16px 32px;font-size:12px;
                          color:#95a5a6;text-align:center;border-top:1px solid #e9ecef}
                </style>
                </head>
                <body>
                <div class="container">
                  <div class="header">
                    <h1>PersonalizedStudy Planner</h1>
                    <p>Task Share Invitation</p>
                  </div>
                  <div class="body">
                    <p style="font-size:16px;color:#2c3e50">Hi <strong>%s</strong>,</p>
                    <p style="font-size:15px;color:#555">A fellow student wants to share a study task with you:</p>
                
                    <div class="sender-card">
                      %s
                      <div>
                        <div class="sender-name">%s %s</div>
                        <div class="sender-email">%s</div>
                      </div>
                    </div>
                
                    <div class="task-card">
                      <p class="task-title">%s</p>
                      <div class="task-row"><span class="task-label">Description:</span>%s</div>
                      <div class="task-row"><span class="task-label">Deadline:</span>%s</div>
                    </div>
                
                    <p style="font-size:14px;color:#555;text-align:center">
                      Would you like to add this task to your Study Planner?
                    </p>
                
                    <div class="actions">
                      <a href="%s" class="btn btn-accept">✅ Accept</a>
                      <a href="%s" class="btn btn-decline">❌ Decline</a>
                    </div>
                
                    <p class="note">
                      These buttons work directly — no login required.<br>
                      You can also respond from the <strong>Notifications</strong> section in your Study Planner.
                    </p>
                  </div>
                  <div class="footer">Automated message from Personalized Study Planner — please do not reply.</div>
                </div>
                </body>
                </html>
                """.formatted(
                recipientFirstName,
                senderAvatar,
                sender.getFirstName(), sender.getLastName(),
                sender.getEmail(),
                task.getTaskName(),
                task.getTaskDescription(),
                deadline,
                acceptUrl,
                declineUrl);
    }

    private String buildReminderEmailBody(String firstName, Task task) {
        String deadline = task.getTaskDeadline() != null
                ? task.getTaskDeadline().format(FORMATTER)
                : "No deadline set";
        String priorityColor = switch (task.getPriority()) {
            case HIGH -> "#e74c3c";
            case MEDIUM -> "#f39c12";
            case LOW -> "#27ae60";
        };
        String statusBadge = switch (task.getStatus()) {
            case PENDING -> "🕐 Pending";
            case IN_PROGRESS -> "🔄 In Progress";
            case COMPLETED -> "✅ Completed";
        };
        return """
                <!DOCTYPE html><html><head><meta charset="UTF-8">
                <style>
                  body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
                  .container{max-width:600px;margin:30px auto;background:#fff;border-radius:8px;
                             overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
                  .header{background:#2c3e50;padding:24px 32px;color:#fff}
                  .body{padding:32px}
                  .task-card{background:#f8f9fa;border-left:4px solid #2c3e50;border-radius:4px;padding:20px 24px;margin-bottom:24px}
                  .task-title{font-size:20px;font-weight:bold;color:#2c3e50;margin:0 0 12px}
                  .task-row{display:flex;margin-bottom:8px;font-size:14px}
                  .task-label{color:#7f8c8d;width:110px;flex-shrink:0}
                  .priority-badge{display:inline-block;padding:2px 10px;border-radius:12px;
                                  color:#fff;font-size:13px;font-weight:bold;background:%s}
                  .footer{background:#f8f9fa;padding:16px 32px;font-size:12px;
                          color:#95a5a6;text-align:center;border-top:1px solid #e9ecef}
                </style></head><body>
                <div class="container">
                  <div class="header"><h1 style="margin:0;font-size:22px">📚 Study Planner</h1>
                    <p style="margin:6px 0 0;color:#bdc3c7;font-size:14px">Task Deadline Reminder</p></div>
                  <div class="body">
                    <p style="font-size:16px;color:#2c3e50">Hi <strong>%s</strong>,</p>
                    <p style="color:#555;font-size:15px">Your task is due <strong>tomorrow</strong>.</p>
                    <div class="task-card">
                      <p class="task-title">%s</p>
                      <div class="task-row"><span class="task-label">Description:</span><span>%s</span></div>
                      <div class="task-row"><span class="task-label">Priority:</span>
                        <span class="priority-badge">%s</span></div>
                      <div class="task-row"><span class="task-label">Status:</span><span>%s</span></div>
                      <div class="task-row"><span class="task-label">Deadline:</span><span>%s</span></div>
                    </div>
                    <div style="background:#fff3cd;border:1px solid #ffc107;border-radius:4px;
                         padding:12px 16px;font-size:14px;color:#856404">
                      ⚠️ <strong>Due tomorrow!</strong> Log in to complete this task.
                    </div>
                  </div>
                  <div class="footer">Automated reminder — please do not reply.</div>
                </div></body></html>
                """
                .formatted(priorityColor, firstName, task.getTaskName(),
                        task.getTaskDescription(), task.getPriority().name(), statusBadge, deadline);
    }

    private String buildWelcomeEmailBody(String firstName) {
        return """
                <!DOCTYPE html><html><head><meta charset="UTF-8">
                <style>
                  body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
                  .container{max-width:600px;margin:30px auto;background:#fff;border-radius:8px;
                             overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
                  .header{background:#2c3e50;padding:32px;text-align:center;color:#fff}
                  .body{padding:36px 32px}
                  .features{background:#f8f9fa;border-radius:6px;padding:20px 24px;margin-bottom:28px}
                  .quote{border-left:4px solid #2c3e50;padding:12px 20px;margin-bottom:24px;
                         font-size:14px;color:#7f8c8d;font-style:italic}
                  .footer{background:#f8f9fa;padding:16px 32px;font-size:12px;
                          color:#95a5a6;text-align:center;border-top:1px solid #e9ecef}
                </style></head><body>
                <div class="container">
                  <div class="header">
                    <h1 style="margin:0 0 8px">📚 Personalized Study Planner</h1>
                    <p style="margin:0;color:#bdc3c7">Your journey to success starts here</p>
                  </div>
                  <div class="body">
                    <p style="font-size:18px;color:#2c3e50">Welcome, <strong>%s</strong>! 🎉</p>
                    <p style="font-size:15px;color:#555;line-height:1.7">
                      We're thrilled to have you on board. With <strong>Personalized Study Planner</strong>,
                      you build habits, set goals, and turn ambitions into achievements.
                    </p>
                    <div class="features">
                      <h3 style="margin:0 0 12px;color:#2c3e50;font-size:15px">✨ Here's what you can do:</h3>
                      <p style="font-size:14px;color:#555;margin:0 0 8px">📝 &nbsp; Create and organize your study tasks</p>
                      <p style="font-size:14px;color:#555;margin:0 0 8px">🎯 &nbsp; Set priorities and track your progress</p>
                      <p style="font-size:14px;color:#555;margin:0 0 8px">⏰ &nbsp; Get deadline reminders before it's too late</p>
                      <p style="font-size:14px;color:#555;margin:0">🤝 &nbsp; Share tasks with your study friends</p>
                    </div>
                    <div class="quote">"The secret of getting ahead is getting started." — Mark Twain</div>
                    <p style="font-size:15px;color:#555">Build not only your studies — but your future as well. 🚀</p>
                  </div>
                  <div class="footer">Automated message — please do not reply.</div>
                </div></body></html>
                """
                .formatted(firstName);
    }
}