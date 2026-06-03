## Personalized Study Planner – Overview

### Project description

The Personalized Study Planner is a full-stack web application that helps students plan, organize, and track their study activities. It combines a modern **React + TypeScript** frontend with a **Spring Boot** backend and a relational database to deliver personalized schedules, progress tracking, and smart integrations (Google OAuth2, email, calendar, and AI-powered assistance). The project was developed over 8 sprints × 2 weeks as part of the Software Production Project course, Spring 2026.

## Product vision

Students today waste time juggling multiple apps — calendars, to-do lists, and reminder tools — instead of actually studying. The Personalized Study Planner exists to eliminate that fragmentation.

Vision statement: A single, intelligent platform where students can manage all their academic responsibilities without switching between tools.
Main goals:
-	Provide a unified task and deadline management system
-	Enable personalized study planning with AI assistance
-	Support multilingual use across Finnish, English, Vietnamese, and Nepali
-	Deliver a clean, accessible UI that does not add friction to the student's workflow


Key capabilities include:
- **User registration & authentication**, including Google OAuth2 login.
- **Personalized study plan creation**, with subjects, tasks, and time slots.
- **Dynamic scheduling & progress tracking**, helping students stay on top of their goals.
- **Integrations** such as email notifications, Google Calendar sync, and AI support for suggestions.
- UI and database-level localization in 4 languages

### High-level flow

1. Users access the **frontend SPA** (served via Nginx) in the browser.
2. The frontend communicates with the **Spring Boot backend** via REST APIs (e.g. `/api/**`, `/oauth2/authorization/**`).
3. The backend authenticates users (username/password + OAuth2), issues JWT tokens, and enforces authorization.
4. Application data (users, study plans, tasks, schedules, etc.) is persisted through **Spring Data JPA** to a **PostgreSQL** database.
5. Background responsibilities include sending email notifications, integrating with Google Calendar, and using AI services to enhance the planning experience.

### Architecture overview

- **Frontend**
  - React + TypeScript + Vite SPA
  - Deployed behind Nginx (also handling API proxying and OAuth2 redirects)
  - See `Frontend/README.md` for details and local run instructions

- **Backend**
  - Spring Boot REST API (`Personalized-Study-Planner` application)
  - Uses Spring Security (JWT + OAuth2), Spring Data JPA, and PostgreSQL
  - Configured via `application.yaml` with externalized environment variables
  - See `Backend/README.md` for service details and environment configuration

- **Data & external services**
  - Relational database (PostgreSQL) for persistent storage
  - SMTP mail server (e.g. Gmail) for notifications
  - Google OAuth2 and Calendar APIs
  - Google GenAI (Gemini) for AI-related features

### ER and Use Case diagrams

The system architecture is backed by:
- An **ER diagram** that models the main entities (User, StudyPlan, Task/Subject, Schedule/Session, etc.) and their relationships.
- A **Use Case diagram** that shows high-level interactions like registration/login, Google sign-in, creating/updating study plans, generating schedules, and tracking progress.

These diagrams live alongside the project documentation and are referenced by the sprint reports.
### Localization

-  Row-based localization: Users may set the language of their choice from the given four options. Language will automatically be synched with the backend and stored in the database. When users create their tasks or generate tasks using LLM's they are stored in the DB and an additional column **Language** was added which shows the language of the user when creating the tasks.
- Additionally the LLM will respond in the language set by the user.

---

## Testing
Sprint 7 focused on comprehensive functional and non-functional testing of the application. The following testing activities were completed:

- **Functional Testing** — Final JUnit unit tests executed after Sprint 6 code cleanup, bug tracking and resolution, Trello user story review, and successful Jenkins build with Docker deployment.
- **Heuristic Evaluation** — The UI was evaluated against Nielsen's 10 Usability Heuristics, with findings and recommendations documented.
- **User Acceptance Testing (UAT)** — All six core user scenarios (authentication, task management, session scheduling, calendar view, localization, and profile management) were tested and accepted.
- **Static Code Analysis** — SonarQube integrated via Jenkins pipeline; all quality gate metrics resolved to grade A or B.
- **Security Testing** — Manual OWASP-based testing covering authentication, authorization, input validation, and API configuration.

## Testing instructions
Run backend unit tests (JUnit 5)
- cd Backend
- ./mvnw test

Access JaCoCo coverage report
- After running tests, open:
- Backend/target/site/jacoco/index.html
  
Run via Jenkins pipeline
The Jenkins pipeline automatically runs on every commit and executes:
1.	Checkout
2.	Build (Maven)
3.	Unit tests (JUnit 5)
4.	SonarQube static analysis
5.	Publish test results
6.	Build and push Docker images
Access Jenkins dashboard at http://localhost:8080 after starting containers.

> Full details of all testing activities, results, and the bug tracking table are available in the **`Sprint Report/`** folder in the project root.

## Sprint Overview

| Sprint | Goal |
|--------|------|
| Sprint 1 | Project planning, vision, backlog creation, Figma UI mockups |
| Sprint 2 | Backend setup, database schema, authentication, CRUD APIs, unit tests |
| Sprint 3 | Frontend implementation, CI/CD Jenkins pipeline, frontend-backend integration |
| Sprint 4 | Docker containerization, full deployment, documentation update |
| Sprint 5 | UI localization (i18next, 4 languages) |
| Sprint 6 | Database localization, static code analysis (SonarQube), code cleanup |
| Sprint 7 | Quality assurance — functional testing, UAT, heuristic evaluation, security testing |
| Sprint 8 | Final documentation, README finalization, presentation |


---

## Sprint 1 – Project Planning & Vision

**Goal:** Define the project foundation — goals, scope, team roles, user stories, and UI mockups.

- Identified problem: students lack a unified study management tool
- Formed team and assigned roles (see Authors section)
- Created initial product backlog in Trello
- Designed UI/UX mockups in Figma
- Defined scope: task management, authentication, calendar integration, optional AI features
- Risk analysis documented (AI complexity, integration risk, time constraints)

---

## Sprint 2 – Requirements & Database

**Goal:** Set up backend, design database schema, implement authentication and core CRUD APIs.

- Designed and implemented PostgreSQL database schema (Users, Tasks, Reminders, Suggested tables)
- Built ER Diagram and Use Case Diagram
- Implemented user registration and login (email/password)
- Developed core CRUD APIs for tasks
- Wrote JUnit 5 unit tests — achieved 92% code coverage (JaCoCo)
- MVC structure established in Spring Boot

| | |
|---|---|
| **Database** | PostgreSQL |
| **Unit testing tools** | JUnit 5, JaCoCo |

**User stories completed:**
- Create personal profile
- Create, edit, delete tasks with deadlines and priorities
- Mark task status (pending / in progress / completed)
- Filter tasks by status

---

## Sprint 3 – UI Implementation & CI

**Goal:** Build the React frontend, connect it to the backend, and set up the Jenkins CI/CD pipeline.

- Implemented all core UI screens: login, signup, dashboard, task management, calendar, profile
- Connected frontend to backend REST APIs
- Implemented JWT authentication and Google OAuth2 sign-in on the frontend
- Set up Jenkins pipeline with stages: Checkout → Build → Test (JUnit) → Code Coverage (JaCoCo) → Publish Test Results → Build and Push Docker Image
- Implemented: task sharing between users, LLM task generation, Google Calendar sync
- Resolved git merge conflicts and API integration bugs

| | |
|---|---|
| **UI framework** | React + TypeScript + Vite + Tailwind CSS |
| **CI tool** | Jenkins |
| **Coverage tool** | JaCoCo |

---

## Sprint 4 – Docker Containerization

**Goal:** Containerize the full application stack and deploy via Docker.

- Created Dockerfiles for both frontend (Nginx + React build) and backend (Spring Boot)
- Configured `compose.yaml` to orchestrate frontend, backend, and PostgreSQL containers
- Verified inter-container communication via Docker networking
- Pushed images to Docker Hub: `mustah21/study-planner-frontend` and `mustah21/study-planner-backend`
- Deployed and tested using Play with Docker
- Finalized GitHub repository structure and README

| | |
|---|---|
| **Services containerized** | Frontend (Nginx), Backend (Spring Boot), Database (PostgreSQL) |
| **Deployment** | Docker Compose / Docker Hub |

---

## Sprint 5 – UI Localization

**Goal:** Implement full UI localization in 4 languages using i18next.

**Supported languages:**

| Language | Owner |
|----------|-------|
| English | All members |
| Finnish | Jari |
| Vietnamese | Minh |
| Nepali | Aashish / Mahi |

**Localization approach:** i18next framework with `useTranslation` React hook. Translation keys defined per component; JSON translation files created per language under native speaker supervision.

**Language switching is available in two places:**
1. Landing page — top center button (visible before login)
2. Profile page — top right corner button (available after login)

Language selection persists across all pages and sessions (stored in localStorage).

---

## Sprint 6 – Database Localization

**Goal:** Extend localization to the database layer and perform static code analysis.

- Added `language` column to the Tasks table to store the user's active language at task creation time
- LLM responses now generated in the user's selected language
- Updated ER Diagram and UML models to reflect schema changes
- UTF-8 encoding enforced across the database
- SonarQube integrated via Jenkins pipeline — quality gate passed with all grades A/B
- Code cleanup: removed unused imports, dead code, magic numbers; added JavaDoc to all public service methods
- 148 unit tests run — 0 failures, 0 errors after refactoring

**SonarQube metrics (Sprint 6):**

| Metric | Value |
|--------|-------|
| Code coverage | 86.4% |
| Duplications | 0.0% |
| Lines of code | 3,204 |
| Functions | 216 |

---

## Sprint 7 – Quality Assurance

**Goal:** Comprehensive functional and non-functional testing before final delivery.

### Functional Testing

- Final JUnit unit tests executed after Sprint 6 code cleanup — all 148 tests pass
- Bug tracking table maintained with 7 identified bugs (4 fixed, 3 under investigation)
- Jenkins build confirmed with Docker deployment

### Heuristic Evaluation

| | |
|---|---|
| **Method** | Nielsen's 10 Usability Heuristics (scale 0–4) |
| **Evaluators** | Aashish Timalsina, Jari Orkolainen — April 2026 |
| **Average severity** | 1.5 / 4 — no critical issues found |

**Key findings:**

| Heuristic | Issue | Severity |
|-----------|-------|----------|
| H1 | Missing save confirmations | 2.5 |
| H5 | No delete confirmation dialog | 2.5 |
| H4 | Inconsistent button labels | 1.0 |

### User Acceptance Testing (UAT)

- 10 test cases executed by all 5 team members — all passed
- Scenarios: task creation, modification, filtering, email notifications, login, delete, mark complete
- **Overall result: ACCEPTED**

### Static Code Analysis

| Category | Issues | Grade |
|----------|--------|-------|
| Security | 0 open | A |
| Reliability | 0 open | A |
| Maintainability | 6 open | A |

---

## Sprint 8 – Documentation & Finalization

**Goal:** Finalize all documentation, complete the GitHub README, and prepare the final presentation.

- Updated and finalized this README according to Sprint 8 requirements
- Organized and completed the `/docs` folder with all sprint reports and diagrams
- Prepared 25-minute final presentation
- Submitted self- and peer-review forms
- Submitted course feedback

> All sprint documentation, including planning notes, retrospective summaries, and design artifacts (ER and Use Case diagrams), is stored in the **Sprint Report** folder in the project root. Use that folder as the primary reference for project history and detailed design decisions.

---

## How to Run the Project

### Prerequisites

| Tool | Version |
|------|---------|
| Docker | 24+ |
| Docker Compose | v2+ |
| Git | Any recent version |

> Java and Node.js are not required locally — everything runs inside Docker containers.

### Environment Setup

**1. Clone the repository:**

```bash
git clone https://github.com/MarcusHoanggg/Personalized-Study-Planner.git
cd Personalized-Study-Planner
```

**2. Create a `.env` file in the root directory:**

```env
POSTGRES_DB=studyplanner
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GEMINI_API_KEY=your_gemini_api_key
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_email_app_password
```

### Run with Docker Compose

```bash
docker compose up --build
```

**Access the application:**

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8081 |
| Jenkins | http://localhost:8080 |
| SonarQube | http://localhost:9000 |

**Stop the application:**

```bash
docker compose down
```

---

## Repository Structure

```
Personalized-Study-Planner/
├── Backend/                          # Spring Boot REST API
│   ├── src/
│   └── README.md
├── Frontend/                         # React + TypeScript SPA
│   ├── src/
│   └── README.md
├── docs/                             # All project documentation
│   ├── Sprint-2-Report.pdf
│   ├── Sprint-3-Report.pdf
│   ├── Sprint-4-Report.pdf
│   ├── Sprint-5-Report.pdf
│   ├── Sprint-6-Report.pdf
│   ├── Sprint-7-Report.pdf
│   ├── Sprint-8-Report.pdf
│   ├── Testing-Documentation.pdf
│   ├── Localization-Setup.pdf   
├── compose.yaml                      # Docker Compose configuration
├── Jenkinsfile                       # Jenkins CI/CD pipeline definition
├── sonar-project.properties          # SonarQube configuration
└── README.md
```

**Subproject READMEs:**
- **Frontend:** see `Frontend/README.md` for UI, tech stack, and run instructions
- **Backend:** see `Backend/README.md` for API, configuration, and backend stack details

---

## Authors

**Course:** Software Production Project (AD) — Spring 2026 · **Group 8**

| Name | Role |
|------|------|
| Aashish Timalsina | Project Lead · Backend Developer · Database Design · Scrum Master (Sprints 4, 7) |
| Megha Kumari | UI/UX Designer (Figma) · Frontend Developer · Scrum Master (Sprints 2, 8) |
| Minh Hoang | Frontend Developer · Scrum Master (Sprint 1) |
| Jari Orkolainen | Backend Developer · Tester · Scrum Master (Sprint 5) |
| Mustafa Ahmad | Backend Developer · DevOps (Jenkins, Docker) · Scrum Master (Sprints 3, 6) |
