package com.studyplanner.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.api.services.calendar.Calendar;
import com.studyplanner.backend.entity.Task;

@DisplayName("CalendarServiceImpl Tests")
class CalendarServiceImplTest {

    @Test
    void pushToCalendar_WhenTokenMissing_ShouldReturnNull() throws Exception {
        CalendarServiceImpl service = new CalendarServiceImpl();
        Task task = Task.builder()
                .taskName("Math")
                .taskDescription("Practice algebra")
                .taskDeadline(LocalDateTime.now().plusDays(1))
                .build();

        String eventId = service.pushToCalendar(task);
        assertNull(eventId);
    }

    @Test
    void createCalendarService_PrivateFactory_ShouldReturnClient() throws Exception {
        CalendarServiceImpl service = new CalendarServiceImpl("dummy-token");
        Method method = CalendarServiceImpl.class.getDeclaredMethod("createCalendarService", String.class);
        method.setAccessible(true);

        Calendar calendar = (Calendar) method.invoke(service, "dummy-token");
        assertNotNull(calendar);
        assertNotNull(calendar.events());
    }
}
