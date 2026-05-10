import QuestionCard from "./QuestionCard.jsx";

function QuizComponent({
  questions,
  answers,
  onAnswerChange,
  onSubmit,
  result,
  submitting,
}) {
  return (
    <div className="page">
      {questions.length ? (
        questions.map((question, index) => (
          <QuestionCard
            key={question.id}
            question={question}
            index={index}
            value={answers[question.id]}
            onChange={onAnswerChange}
          />
        ))
      ) : (
        <p className="muted">currently nothing here</p>
      )}

      <button
        type="button"
        className="btn btn-primary"
        onClick={onSubmit}
        disabled={submitting}
      >
        {submitting ? "Submitting..." : "Submit Answers"}
      </button>

      {result ? (
        <section className="card">
          <h3 className="card-title">Evaluation Result</h3>
          <p>
            Score: <strong>{result.score ?? "currently nothing here"}</strong>
            {result.maxScore != null ? ` / ${result.maxScore}` : ""}
          </p>
          {result.feedback ? (
            <p className="muted">Feedback: {result.feedback}</p>
          ) : null}
        </section>
      ) : null}
    </div>
  );
}

export default QuizComponent;
