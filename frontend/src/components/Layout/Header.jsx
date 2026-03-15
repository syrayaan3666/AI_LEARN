import { useLocation } from "react-router-dom";
import { useAppContext } from "../../context/AppContext.jsx";

const titleMap = {
  "/dashboard": "Dashboard",
  "/topics": "Topics",
  "/quiz": "Quiz",
  "/quiz-history": "Quiz History",
  "/doubts": "Doubts Assistant",
  "/curriculum": "AI Curriculum",
};

function Header() {
  const { pathname } = useLocation();
  const { studentId } = useAppContext();
  const pageTitle = pathname.startsWith("/learning/")
    ? "Learning"
    : titleMap[pathname] || "AI Learn";

  return (
    <header className="header card">
      <div>
        <h1 className="header-title">{pageTitle}</h1>
        <p className="muted">Personalized AI-powered education flow</p>
      </div>
      <div className="header-user">
        <span className="muted">Student ID</span>
        <strong>{studentId}</strong>
      </div>
    </header>
  );
}

export default Header;
