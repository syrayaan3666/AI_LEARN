import ProgressBar from "../Shared/ProgressBar.jsx";

function TopicCard({ topic, onLearn, onGenerateQuiz, onAskDoubt }) {
  const hasProgress = typeof topic.progress === "number";

  return (
    <article className="card topic-card">
      <div className="topic-head">
        <h3>{topic.name || "currently nothing here"}</h3>
        <span className="badge badge-success">
          {topic.status || "currently nothing here"}
        </span>
      </div>
      <p className="muted">{topic.description || "currently nothing here"}</p>
      {hasProgress ? (
        <ProgressBar value={topic.progress} />
      ) : (
        <p className="muted">currently nothing here</p>
      )}
      <div className="topic-actions">
        <button type="button" className="btn btn-primary" onClick={onLearn}>
          Learn
        </button>
        <button
          type="button"
          className="btn btn-muted"
          onClick={onGenerateQuiz}
        >
          Generate Quiz
        </button>
        <button type="button" className="btn btn-muted" onClick={onAskDoubt}>
          Ask Doubt
        </button>
      </div>
    </article>
  );
}

export default TopicCard;
