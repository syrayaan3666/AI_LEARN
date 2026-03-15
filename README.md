# AI_LEARN

AI_LEARN is an AI-powered learning platform with a Spring Boot backend and React (Vite) frontend.
It supports curriculum generation, topic-based learning notes, video link generation, quizzes, and doubt workflows.

## Tech Stack

- Backend: Java 17, Spring Boot 3.3.5, Spring Data JPA, MySQL
- Frontend: React 19, Vite 7, Axios, React Router
- AI Integrations: CurricuForge endpoints + Groq (for notes/video links)

## Project Structure

- `backend/backend` - Spring Boot API
- `frontend` - React web app

## Prerequisites

- Java 17+
- Maven Wrapper (included)
- Node.js 18+
- MySQL running locally

## Backend Setup

1. Configure MySQL database:
   - Database name: `ai_learning_platform`
   - Update credentials in `backend/backend/src/main/resources/application.properties` if needed.

2. Add local secrets in `backend/backend/.env`:

```properties
GROQ_API_KEY=your_groq_api_key
```

3. Run backend:

```powershell
cd backend/backend
.\mvnw.cmd spring-boot:run
```

Backend runs on `http://localhost:8080`.

## Frontend Setup

1. Install dependencies:

```powershell
cd frontend
npm.cmd install
```

2. Start dev server:

```powershell
npm.cmd run dev
```

Frontend runs on `http://localhost:5173`.

## Key Features

- Curriculum generation (semester/personal planners)
- Topic extraction and grouped topic view
- Structured AI notes generation (JSON-backed)
- AI-generated topic video links
- Quiz generation and history
- Doubt support flow

## Notes

- API keys are loaded from `.env`; do not commit secrets.
- `backend/backend/.gitignore` excludes `.env`.
- Generated notes and video links are persisted and reloaded on revisit.
