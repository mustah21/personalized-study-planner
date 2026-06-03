package com.studyplanner.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HealthController Tests")
class HealthControllerTest {

    private final HealthController controller = new HealthController();

    @Test
    void healthEndpoints_ShouldReturnExpectedMessages() {
        assertEquals("Backend is running ", controller.health());
        assertEquals("Hello, Secured!", controller.secured());
    }
}
