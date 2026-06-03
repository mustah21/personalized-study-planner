# Personalized Study Planner – Frontend

### Description

The frontend is a **React + TypeScript + Vite** single-page application that provides the user interface for the Personalized Study Planner. It allows students to register/login (including Google OAuth2), configure their study preferences, view personalized schedules, and track progress through an intuitive, responsive UI.

### How to run the frontend

- **Prerequisites**
  - **Node.js** (LTS, e.g. 18+)
  - **npm** or **yarn**

- **Install dependencies**

```bash
cd Frontend
npm install
```

- **Run in development mode**

```bash
npm run dev
```

By default the app runs at `http://localhost:5173`. Ensure the backend is running and the environment variables (e.g. `VITE_API_URL`, `VITE_FRONTEND_URL`) are correctly configured.

- **Build for production**

```bash
npm run build
```

This produces a static bundle in `dist/`, which is served by Nginx in the Dockerized setup.

### Technology stack

- **Framework**: React, TypeScript
- **Build tool**: Vite
- **UI**: Modern component-based SPA (React hooks, functional components)
- **HTTP**: RESTful API calls to the Spring Boot backend
- **Containerization**: Nginx serving the built assets in Docker

### Architecture design (ER & Use Case diagrams)

- **Entity–Relationship (ER) diagram**  
  The ER diagram models the core domain entities such as **User**, **StudyPlan**, **Task/Subject**, and related entities (e.g. progress tracking, schedules). It shows how a user owns one or more study plans, which in turn contain tasks/sessions linked to subjects and time slots.

- **Use Case diagram**  
  The use case diagram captures key interactions like **Register/Login**, **Connect with Google**, **Create/Update/Delete Study Plan**, **View Timetable**, and **Track Progress**. It gives a high-level view of how students and, optionally, admins interact with the system.

Both diagrams are stored alongside the project documentation and are referenced by the sprint reports.

### Sprint reports

Sprint reports (including detailed progress, planning, and artifacts such as ER and Use Case diagrams) are available in the **Sprint Report** folder in the project root. Refer to that folder for iteration-by-iteration documentation and design evolution.

