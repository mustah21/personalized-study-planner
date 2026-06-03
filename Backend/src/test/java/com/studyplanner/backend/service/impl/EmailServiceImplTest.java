package com.studyplanner.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    void sendTaskReminderEmail_ShouldBuildAndSendMessage() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        Task task = Task.builder()
                .taskName("Physics practice")
                .taskDescription("Solve chapter 2 numericals")
                .taskDeadline(LocalDateTime.now().plusDays(1))
                .priority(Task.Priority.HIGH)
                .status(Task.Status.PENDING)
                .build();

        emailService.sendTaskReminderEmail("student@example.com", "Student", task);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        MimeMessage sent = captor.getValue();
        assertTrue(sent.getSubject().contains("Physics practice"));
    }

    @Test
    void sendWelcomeAndShareInvite_ShouldSendMessages() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        emailService.sendWelcomeEmail("new@example.com", "New User");

        User sender = User.builder().firstName("Aashish").lastName("Dev").email("a@example.com").build();
        Task task = Task.builder().taskName("Group revision").taskDescription("Read together").build();
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        emailService.sendShareInviteEmail(
                "friend@example.com", "Friend", sender, task, "http://accept", "http://decline");

        verify(mailSender, times(2)).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

}
