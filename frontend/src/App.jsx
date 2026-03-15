import { Navigate, Route, Routes } from "react-router-dom";
import Sidebar from "./components/Layout/Sidebar.jsx";
import Header from "./components/Layout/Header.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import TopicsPage from "./pages/TopicsPage.jsx";
import LearningPage from "./pages/LearningPage.jsx";
import QuizPage from "./pages/QuizPage.jsx";
import QuizHistoryPage from "./pages/QuizHistoryPage.jsx";
import DoubtPage from "./pages/DoubtPage.jsx";
import CurriculumPage from "./pages/CurriculumPage.jsx";
import "./App.css";

function App() {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-main">
        <Header />
        <main className="app-content">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/topics" element={<TopicsPage />} />
            <Route path="/learning/:topicId" element={<LearningPage />} />
            <Route path="/quiz" element={<QuizPage />} />
            <Route path="/quiz-history" element={<QuizHistoryPage />} />
            <Route path="/doubts" element={<DoubtPage />} />
            <Route path="/curriculum" element={<CurriculumPage />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

export default App;
