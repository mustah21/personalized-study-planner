package com.studyplanner.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class PersonalizedStudyPlannerApplicationTests {

	@Test
	@Disabled("Requires running PostgreSQL - skipped in CI")
	void contextLoads() {
	}

}
