package com.studyplanner.backend.service.impl;


import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.service.CalendarService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
@NoArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private String accessToken;

    public CalendarServiceImpl(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String pushToCalendar(Task task) throws IOException {

        if (this.accessToken == null) {
            log.warn("No Google Access Token found. Skipping calendar sync.");
            return null;
        }

        Calendar service = createCalendarService(this.accessToken);

        Event event = new Event()
                .setSummary(task.getTaskName())
                .setDescription(task.getTaskDescription());

        DateTime startDateTime = new DateTime(task.getTaskDeadline().toString() + "Z");
        EventDateTime eventTime = new EventDateTime().setDateTime(startDateTime);
        event.setStart(eventTime).setEnd(eventTime);

        return service.events().insert("primary", event).execute().getId();

    }

    private Calendar createCalendarService(String token) {
        AccessToken tokenObj = new AccessToken(token, null);

        GoogleCredentials credentials = GoogleCredentials.create(tokenObj);

        return new Calendar.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Study Planner")
                .build();
    }


}