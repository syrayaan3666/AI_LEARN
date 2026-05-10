import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import QuizComponent from "../components/Quiz/QuizComponent.jsx";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import { generateQuiz, submitQuiz } from "../services/quizService.js";
import { getTopics } from "../services/topicService.js";

function QuizPage() {
  const { studentId, activeTopic } = useAppContext();
  const [searchParams] = useSearchParams();
  const initialTopicId = searchParams.get("topicId") || activeTopic?.id || "";

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [result, setResult] = useState(null);
  const [topics, setTopics] = useState([]);
  const [currentTopicId, setCurrentTopicId] = useState(initialTopicId);

  useEffect(() => {
    async function loadTopics() {
      try {
        const t = await getTopics();
        setTopics(Array.isArray(t) ? t : []);
      } catch {
        setTopics([]);
      }
    }
    loadTopics();
  }, []);

  useEffect(() => {
    async function loadQuiz() {
      setLoading(true);
      setResult(null);
      setAnswers({});
      try {
        if (!currentTopicId) {
          setQuestions([]);
          return;
        }
        const data = await generateQuiz({ topicId: currentTopicId, studentId });
        const payload = data?.questions || data;
        setQuestions(Array.isArray(payload) ? payload : []);
      } catch {
        setQuestions([]);
      } finally {
        setLoading(false);
      }
    }
    loadQuiz();
  }, [currentTopicId, studentId]);

  function handleAnswerChange(questionId, value) {
    setAnswers((prev) => ({ ...prev, [questionId]: value }));
  }

  function handleSelectTopic(id) {
    setCurrentTopicId(id);
  }

  const answerList = useMemo(
    () =>
      Object.entries(answers).map(([questionId, answer]) => ({
        questionId: Number(questionId),
        answer,
      })),
    [answers],
  );

  async function handleSubmit() {
    setSubmitting(true);
    try {
      const data = await submitQuiz({
        studentId,
        topicId: currentTopicId,
        answers: answerList,
      });
      setResult(data?.result || data || null);
    } catch {
      setResult({
        score: "currently nothing here",
        feedback: "currently nothing here",
      });
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <LoadingSpinner label="Generating quiz" />;
  }

  return (
    <div className="page">
      <h2 className="page-title">Quiz Practice</h2>
      <p className="muted">
        Topic: {currentTopicId || "currently nothing here"}
      </p>

      <section style={{ marginBottom: "1rem" }}>
        <strong>Topics:</strong>
        <div
          style={{
            display: "flex",
            gap: "0.5rem",
            marginTop: "0.5rem",
            flexWrap: "wrap",
          }}
        >
          {topics.length === 0 ? (
            <span className="muted">No topics available</span>
          ) : (
            topics.map((t) => (
              <button
                key={t.id}
                type="button"
                className={
                  String(t.id) === String(currentTopicId)
                    ? "btn btn-primary"
                    : "btn btn-muted"
                }
                onClick={() => handleSelectTopic(t.id)}
              >
                {t.name}
              </button>
            ))
          )}
        </div>
      </section>
      <QuizComponent
        questions={questions}
        answers={answers}
        onAnswerChange={handleAnswerChange}
        onSubmit={handleSubmit}
        result={result}
        submitting={submitting}
      />
    </div>
  );
}

export default QuizPage;
