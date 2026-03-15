import { useEffect, useState } from "react";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import { getQuizHistory } from "../services/quizService.js";

function QuizHistoryPage() {
  const { studentId } = useAppContext();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadHistory() {
      setLoading(true);
      try {
        const data = await getQuizHistory(studentId);
        setHistory(Array.isArray(data) ? data : []);
      } catch {
        setHistory([]);
      } finally {
        setLoading(false);
      }
    }
    loadHistory();
  }, [studentId]);

  if (loading) {
    return <LoadingSpinner label="Loading quiz history" />;
  }

  return (
    <div className="page">
      <h2 className="page-title">Quiz History</h2>
      <section className="card">
        <div className="table-head">
          <strong>Topic</strong>
          <strong>Score</strong>
          <strong>Difficulty</strong>
          <strong>Date Attempted</strong>
        </div>
        {history.length ? (
          history.map((item, index) => (
            <div
              key={item.id || `${item.topic || "topic"}-${index}`}
              className="table-row"
            >
              <span>{item.topic || "currently nothing here"}</span>
              <span>
                {item.score != null && item.maxScore != null
                  ? `${item.score}/${item.maxScore}`
                  : item.score || "currently nothing here"}
              </span>
              <span>{item.difficulty || "currently nothing here"}</span>
              <span className="muted">
                {item.date || item.dateAttempted || "currently nothing here"}
              </span>
            </div>
          ))
        ) : (
          <div className="table-row">
            <span>currently nothing here</span>
            <span>currently nothing here</span>
            <span>currently nothing here</span>
            <span className="muted">currently nothing here</span>
          </div>
        )}
      </section>
    </div>
  );
}

export default QuizHistoryPage;
