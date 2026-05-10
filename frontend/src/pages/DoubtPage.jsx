import { useEffect, useState } from "react";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import { askDoubt, getDoubtHistory } from "../services/doubtService.js";
import { getTopics } from "../services/topicService.js";

function DoubtPage() {
  const { studentId } = useAppContext();

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [topics, setTopics] = useState([]);
  const [currentTopicId, setCurrentTopicId] = useState(null);
  const [currentTopic, setCurrentTopic] = useState(null);
  const [doubtText, setDoubtText] = useState("");
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    async function loadTopics() {
      setLoading(true);
      try {
        const data = await getTopics();
        setTopics(data || []);
        const data_history = await getDoubtHistory(studentId);
        setHistory(data_history || []);
      } catch (err) {
        console.error("Error loading data:", err);
        setTopics([]);
        setHistory([]);
      } finally {
        setLoading(false);
      }
    }
    loadTopics();
  }, [studentId]);

  const handleTopicSelect = (topic) => {
    setCurrentTopicId(topic.id);
    setCurrentTopic(topic);
    setDoubtText("");
    setResult(null);
  };

  const handleSubmitDoubt = async () => {
    if (!doubtText.trim() || !currentTopicId) return;

    setSubmitting(true);
    try {
      const response = await askDoubt({
        studentId,
        topicId: currentTopicId,
        doubtText: doubtText,
      });
      setResult({
        doubtId: response.doubtId,
        answer: response.answer || response.aiResponse,
        doubtText: doubtText,
      });
      setDoubtText("");
      // Refresh history after successful submission
      await loadDoubtHistory();
    } catch (err) {
      console.error("Error submitting doubt:", err);
      setResult({
        error: "Failed to get answer. Please try again.",
        doubtText: doubtText,
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleClearSelection = () => {
    setCurrentTopicId(null);
    setCurrentTopic(null);
    setDoubtText("");
    setResult(null);
    // Refresh history when going back to topic selection
    loadDoubtHistory();
  };

  const loadDoubtHistory = async () => {
    try {
      const data = await getDoubtHistory(studentId);
      setHistory(data || []);
    } catch (err) {
      console.error("Error loading doubt history:", err);
    }
  };

  if (loading) {
    return <LoadingSpinner label="Loading topics and doubt history" />;
  }

  return (
    <div className="page">
      <h2 className="page-title">Ask Your Doubts</h2>
      <p className="muted">Select a topic to ask your doubt about it</p>

      {!currentTopicId ? (
        // Topic Selection View
        <div className="topics-grid">
          {topics.map((topic) => (
            <button
              key={topic.id}
              className="topic-button"
              onClick={() => handleTopicSelect(topic)}
            >
              <strong>{topic.name}</strong>
              <small>{topic.description?.substring(0, 60)}...</small>
            </button>
          ))}
        </div>
      ) : (
        // Doubt Input View
        <div className="doubt-form-container">
          <div className="doubt-form-header">
            <h3>{currentTopic?.name}</h3>
            <button
              className="back-button"
              onClick={handleClearSelection}
              disabled={submitting}
            >
              ← Back to Topics
            </button>
          </div>

          {!result ? (
            <div className="doubt-input-area">
              <textarea
                className="doubt-textarea"
                placeholder="Write your doubt here..."
                value={doubtText}
                onChange={(e) => setDoubtText(e.target.value)}
                disabled={submitting}
                rows={6}
              />
              <div className="button-group">
                <button
                  className="submit-button"
                  onClick={handleSubmitDoubt}
                  disabled={!doubtText.trim() || submitting}
                >
                  {submitting ? "Generating Answer..." : "Submit Doubt"}
                </button>
              </div>
            </div>
          ) : (
            <div className="result-card">
              <div className="result-header">
                <h4>Your Doubt:</h4>
                <p>{result.doubtText}</p>
              </div>
              <div className="result-body">
                <h4>AI Response:</h4>
                <p className="ai-answer">{result.answer || result.error}</p>
              </div>
              <button
                className="new-doubt-button"
                onClick={() => {
                  setResult(null);
                  setDoubtText("");
                }}
              >
                Ask Another Doubt on This Topic
              </button>
              <button className="back-button" onClick={handleClearSelection}>
                ← Back to Topics
              </button>
            </div>
          )}
        </div>
      )}

      {/* Doubt History Section */}
      {history.length > 0 && (
        <div className="doubt-history">
          <h3>Doubt History</h3>
          <div className="history-list">
            {history.map((item, idx) => (
              <div key={idx} className="history-item">
                <small className="history-date">
                  {item.createdAt
                    ? new Date(item.createdAt).toLocaleDateString()
                    : "Date unknown"}
                </small>
                <p>
                  <strong>Q:</strong> {item.doubtText || item.question}
                </p>
                <p>
                  <strong>A:</strong> {item.aiResponse?.substring(0, 150)}...
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default DoubtPage;
