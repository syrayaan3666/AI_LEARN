import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import ProgressBar from "../components/Shared/ProgressBar.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import { getTopics } from "../services/topicService.js";
import { getQuizHistory } from "../services/quizService.js";

function DashboardPage() {
  const { studentId } = useAppContext();
  const [loading, setLoading] = useState(true);
  const [topics, setTopics] = useState([]);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    async function loadDashboard() {
      setLoading(true);
      try {
        const [topicsData, historyData] = await Promise.all([
          getTopics(),
          getQuizHistory(studentId),
        ]);
        setTopics(Array.isArray(topicsData) ? topicsData : []);
        setHistory(Array.isArray(historyData) ? historyData : []);
      } catch {
        setTopics([]);
        setHistory([]);
      } finally {
        setLoading(false);
      }
    }
    loadDashboard();
  }, [studentId]);

  const averageProgress = useMemo(() => {
    if (!topics.length) {
      return 0;
    }
    const total = topics.reduce((acc, topic) => acc + (topic.progress || 0), 0);
    return Math.round(total / topics.length);
  }, [topics]);

  if (loading) {
    return <LoadingSpinner label="Loading dashboard" />;
  }

  return (
    <div className="page">
      <h2 className="page-title">Student Progress Overview</h2>

      <section className="grid grid-cols-3">
        <article className="card">
          <h3 className="card-title">Overall Progress</h3>
          {topics.length ? (
            <ProgressBar value={averageProgress} />
          ) : (
            <p className="muted">currently nothing here</p>
          )}
        </article>
        <article className="card">
          <h3 className="card-title">Quizzes Attempted</h3>
          <p style={{ fontSize: "1.5rem", fontWeight: 700 }}>
            {history.length || "currently nothing here"}
          </p>
        </article>
        <article className="card">
          <h3 className="card-title">Topics In Progress</h3>
          <p style={{ fontSize: "1.5rem", fontWeight: 700 }}>
            {topics.length || "currently nothing here"}
          </p>
        </article>
      </section>

      <section className="card">
        <h3 className="card-title">Recently Attempted Quizzes</h3>
        <div className="list-block">
          {history.length ? (
            history.slice(0, 4).map((item, index) => (
              <div
                key={item.id || `${item.topic || "topic"}-${index}`}
                className="list-item"
              >
                <span>{item.topic || "currently nothing here"}</span>
                <span>{item.score || "currently nothing here"}</span>
                <span className="muted">
                  {item.date || item.dateAttempted || "currently nothing here"}
                </span>
              </div>
            ))
          ) : (
            <p className="muted">currently nothing here</p>
          )}
        </div>
      </section>

      <section className="card">
        <h3 className="card-title">Quick Navigation</h3>
        <div className="quick-actions">
          <Link to="/topics" className="btn btn-primary">
            Explore Topics
          </Link>
          <Link to="/quiz" className="btn btn-muted">
            Start Quiz
          </Link>
          <Link to="/doubts" className="btn btn-muted">
            Ask Doubt
          </Link>
        </div>
      </section>
    </div>
  );
}

export default DashboardPage;
