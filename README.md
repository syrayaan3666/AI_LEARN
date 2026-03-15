# AI_LEARN

## Short Description
AI_LEARN is an AI-powered learning platform that generates personalized curricula, converts them into topics, creates structured study notes and video links with Groq, and supports quizzes and doubt handling.

## Features
- Curriculum generation (Semester and Personal planners)
- Save curriculum as structured learning topics
- Topic-wise AI notes generation (JSON-backed)
- AI-generated video links per topic
- Quiz generation, submission, and history
- Doubt ask and history flow

## Tech Stack / Technologies Used
- Backend: Java 17, Spring Boot 3.3.5, Spring Data JPA, MySQL
- Frontend: React 19, Vite 7, Axios, React Router
- AI: CurricuForge integration + Groq API

## Project Structure
```text
AI_LEARN/
   backend/
      backend/                 # Spring Boot backend
   frontend/                  # React + Vite frontend
   README.md
```

## Installation / Setup Instructions
### Prerequisites
- Java 17+
- Node.js 18+
- MySQL running locally

### 1) Database setup
- Create database: `ai_learning_platform`
- Update DB config if needed in `backend/backend/src/main/resources/application.properties`

### 2) Backend environment setup
Create `backend/backend/.env`:
```properties
GROQ_API_KEY=your_groq_api_key
```

### 3) Install frontend dependencies
```powershell
cd frontend
npm.cmd install
```

## How to Run the Project
### Run backend
```powershell
cd backend/backend
.\mvnw.cmd spring-boot:run
```
Backend URL: `http://localhost:8080`

### Run frontend
```powershell
cd frontend
npm.cmd run dev
```
Frontend URL: `http://localhost:5173`

## API Endpoints
### Curriculum
- `POST /curriculum/generate`
- `POST /curriculum/generate-mock`
- `POST /curriculum/refine`
- `GET /curriculum/{studentId}`
- `POST /curriculum/{curriculumId}/save-topics`

### Topics / Learning
- `GET /topics`
- `GET /learning/{topicId}`
- `POST /learning/{topicId}/generate-notes`

### Quiz
- `POST /quiz/generate`
- `POST /quiz/submit`
- `GET /quiz/history/{studentId}`

### Doubts
- `POST /doubts/ask`
- `GET /doubts/history/{studentId}`

## Screenshots or Demo
- Add screenshots/GIFs of Dashboard, Curriculum, Learning Notes, and Quiz pages.

## Future Improvements / Roadmap
- Better notes formatting and section-level rendering polish
- Prompt chaining for deeper notes quality
- Enhanced quiz intelligence and adaptive difficulty
- Production deployment and CI/CD pipeline

## Author / Credits
- Rayaan
