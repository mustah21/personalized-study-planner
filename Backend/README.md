# Personalized Study Planner – Backend

### Description

The backend is a **Spring Boot** application that powers the Personalized Study Planner. It exposes REST APIs for user registration and authentication (including Google OAuth2 and JWT), study-plan management, scheduling, and progress tracking. It also integrates with external services such as email and Google Calendar to deliver reminders and a richer planning experience.

### Technology stack

- **Language / Framework**: Java, Spring Boot
- **Modules**: Spring Web, Spring Security (OAuth2 + JWT), Spring Data JPA
- **Database**: PostgreSQL (configurable via `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- **Messaging/Email**: SMTP email support via Spring Mail
- **AI Integration**: Google GenAI (Gemini) configured via `spring.ai.*` properties
- **Build / Run**: Maven or Gradle (depending on your project setup), Docker for containerized deployment

### Architecture design (ER & Use Case diagrams)

- **Entity–Relationship (ER) diagram**  
  The ER diagram defines domain entities such as **User**, **StudyPlan**, **Task/Subject**, **Schedule/Session**, and related tables. Relationships express how a user owns multiple study plans that are composed of tasks/sessions tied to dates, times, and subjects.

- **Use Case diagram**  
  The use case diagram illustrates backend responsibilities behind flows like **Register/Login**, **Google OAuth2 callback handling**, **Create/Update/Delete Study Plan**, **Generate Personalized Schedule**, and **Send Notifications**. It clarifies how external actors (students, admins, third-party services) interact with backend APIs.

The diagrams are maintained with the project documentation and referenced by the sprint reports.

### Sprint reports

Sprint-level documentation, including detailed design notes, updated ER and Use Case diagrams, and implementation progress, is maintained in the **Sprint Report** folder at the project root. Consult that folder for iteration-by-iteration backend evolution and decisions.

