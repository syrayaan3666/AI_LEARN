import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import QuizComponent from "../components/Quiz/QuizComponent.jsx";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import { generateQuiz, submitQuiz } from "../services/quizService.js";

function QuizPage() {
  const { studentId, activeTopic } = useAppContext();
  const [searchParams] = useSearchParams();
  const topicId = searchParams.get("topicId") || activeTopic?.id || "";

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [result, setResult] = useState(null);

  useEffect(() => {
    async function loadQuiz() {
      setLoading(true);
      setResult(null);
      setAnswers({});
      try {
        if (!topicId) {
          setQuestions([]);
          return;
        }
        const data = await generateQuiz({ topicId, studentId });
        const payload = data?.questions || data;
        setQuestions(Array.isArray(payload) ? payload : []);
      } catch {
        setQuestions([]);
      } finally {
        setLoading(false);
      }
    }
    loadQuiz();
  }, [topicId, studentId]);

  function handleAnswerChange(questionId, value) {
    setAnswers((prev) => ({ ...prev, [questionId]: value }));
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
        topicId,
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
      <p className="muted">Topic: {topicId || "currently nothing here"}</p>
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
