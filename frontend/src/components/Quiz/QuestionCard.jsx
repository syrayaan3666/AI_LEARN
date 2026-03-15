function QuestionCard({ question, index, value, onChange }) {
  const isMcq = question.type === "MCQ";

  return (
    <section className="card">
      <h3 className="card-title">
        Q{index + 1}. {question.question}
      </h3>
      {isMcq ? (
        <div className="quiz-options">
          {(question.options || []).map((option) => (
            <label key={option} className="quiz-option">
              <input
                type="radio"
                name={`q-${question.id}`}
                checked={value === option}
                onChange={() => onChange(question.id, option)}
              />
              <span>{option}</span>
            </label>
          ))}
        </div>
      ) : (
        <textarea
          className="textarea"
          placeholder="Write your descriptive answer"
          value={value || ""}
          onChange={(event) => onChange(question.id, event.target.value)}
        />
      )}
    </section>
  );
}

export default QuestionCard;
