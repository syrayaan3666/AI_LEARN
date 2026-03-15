import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import {
  getLearningByTopic,
  generateNotesForTopic,
} from "../services/topicService.js";

function LearningPage() {
  const navigate = useNavigate();
  const { topicId } = useParams();
  const { activeTopic } = useAppContext();
  const [loading, setLoading] = useState(true);
  const [generatingNotes, setGeneratingNotes] = useState(false);
  const [showVideoSection, setShowVideoSection] = useState(false);
  const [content, setContent] = useState(null);

  function normalizeNotesJson(value) {
    if (!value || typeof value !== "object") {
      return null;
    }
    return value;
  }

  function renderStructuredNotes(notesJson) {
    if (!notesJson) {
      return null;
    }

    const concepts = Array.isArray(notesJson.core_concepts)
      ? notesJson.core_concepts
      : [];
    const examples = Array.isArray(notesJson.real_world_examples)
      ? notesJson.real_world_examples
      : [];
    const steps = Array.isArray(notesJson.step_by_step)
      ? notesJson.step_by_step
      : [];
    const terms = Array.isArray(notesJson.key_terms) ? notesJson.key_terms : [];
    const mistakes = Array.isArray(notesJson.common_mistakes)
      ? notesJson.common_mistakes
      : [];
    const questions = Array.isArray(notesJson.practice_questions)
      ? notesJson.practice_questions
      : [];

    return (
      <div style={{ display: "flex", flexDirection: "column", gap: "0.9rem" }}>
        {notesJson.introduction && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>Introduction</h4>
            <p className="muted" style={{ whiteSpace: "pre-wrap" }}>
              {notesJson.introduction}
            </p>
          </section>
        )}

        {concepts.length > 0 && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>Core Concepts</h4>
            {concepts.map((concept, index) => (
              <article
                key={`${concept?.name || "concept"}-${index}`}
                style={{ marginBottom: "0.7rem" }}
              >
                <h5 style={{ marginBottom: "0.2rem" }}>
                  {concept?.name || `Concept ${index + 1}`}
                </h5>
                <p
                  className="muted"
                  style={{ marginBottom: "0.2rem", whiteSpace: "pre-wrap" }}
                >
                  {concept?.explanation || ""}
                </p>
                {Array.isArray(concept?.key_points) &&
                  concept.key_points.length > 0 && (
                    <ul>
                      {concept.key_points.map((point, pointIndex) => (
                        <li key={`${index}-${pointIndex}`}>{point}</li>
                      ))}
                    </ul>
                  )}
              </article>
            ))}
          </section>
        )}

        {examples.length > 0 && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>Real-World Examples</h4>
            <ul>
              {examples.map((example, index) => (
                <li key={`${example?.title || "example"}-${index}`}>
                  <strong>{example?.title || `Example ${index + 1}`}: </strong>
                  {example?.description || ""}
                </li>
              ))}
            </ul>
          </section>
        )}

        {steps.length > 0 && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>
              Step-by-Step Learning Path
            </h4>
            <ol>
              {steps.map((step, index) => (
                <li key={`step-${index}`}>{step}</li>
              ))}
            </ol>
          </section>
        )}

        {terms.length > 0 && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>Key Terms</h4>
            <ul>
              {terms.map((term, index) => (
                <li key={`${term?.term || "term"}-${index}`}>
                  <strong>{term?.term || `Term ${index + 1}`}: </strong>
                  {term?.definition || ""}
                </li>
              ))}
            </ul>
          </section>
        )}

        {mistakes.length > 0 && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>Common Mistakes</h4>
            <ul>
              {mistakes.map((mistake, index) => (
                <li key={`mistake-${index}`}>{mistake}</li>
              ))}
            </ul>
          </section>
        )}

        {questions.length > 0 && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>Practice Questions</h4>
            {questions.map((question, index) => (
              <article
                key={`question-${index}`}
                style={{ marginBottom: "0.6rem" }}
              >
                <p style={{ marginBottom: "0.2rem" }}>
                  <strong>Q{index + 1}.</strong> {question?.question || ""}
                </p>
                {question?.answer && (
                  <p className="muted" style={{ whiteSpace: "pre-wrap" }}>
                    <strong>Answer:</strong> {question.answer}
                  </p>
                )}
              </article>
            ))}
          </section>
        )}

        {notesJson.summary && (
          <section>
            <h4 style={{ marginBottom: "0.35rem" }}>Summary</h4>
            <p className="muted" style={{ whiteSpace: "pre-wrap" }}>
              {notesJson.summary}
            </p>
          </section>
        )}
      </div>
    );
  }

  async function fetchContent(forceGenerate = false) {
    setLoading(true);
    try {
      const data = await getLearningByTopic(topicId, { forceGenerate });
      const notes = data?.notes || data?.generatedNotes || "";
      const resolvedVideoLinks = Array.isArray(data?.videoLinks)
        ? data.videoLinks
        : [];
      setContent({
        topicTitle:
          data?.topicTitle || activeTopic?.name || "currently nothing here",
        notes: notes || "currently nothing here",
        notesJson: normalizeNotesJson(data?.notesJson),
        videoLinks: resolvedVideoLinks,
      });
      setShowVideoSection(resolvedVideoLinks.length > 0);
    } catch {
      setContent({
        topicTitle: activeTopic?.name || "currently nothing here",
        notes: "currently nothing here",
        notesJson: null,
        videoLinks: [],
      });
      setShowVideoSection(false);
    } finally {
      setLoading(false);
    }
  }

  async function handleGenerateNotes() {
    setGeneratingNotes(true);
    setShowVideoSection(true);
    try {
      const data = await generateNotesForTopic(topicId);
      const notes = data?.notes || data?.generatedNotes || "";
      setContent({
        topicTitle:
          data?.topicTitle || activeTopic?.name || "currently nothing here",
        notes: notes || "currently nothing here",
        notesJson: normalizeNotesJson(data?.notesJson),
        videoLinks: Array.isArray(data?.videoLinks) ? data.videoLinks : [],
      });
    } catch (error) {
      // Show error in notes
      setContent((prev) => ({
        ...prev,
        notes: error?.message || "Failed to generate notes. Please try again.",
      }));
    } finally {
      setGeneratingNotes(false);
    }
  }

  useEffect(() => {
    fetchContent(false);
  }, [topicId]);

  if (loading) {
    return <LoadingSpinner label="Loading learning content" />;
  }

  return (
    <div className="page">
      <h2 className="page-title">{content?.topicTitle}</h2>

      <section className="card">
        <h3 className="card-title">Generated Notes</h3>
        {content?.notesJson ? (
          renderStructuredNotes(content.notesJson)
        ) : (
          <p className="muted" style={{ whiteSpace: "pre-wrap" }}>
            {content?.notes}
          </p>
        )}
        <button
          type="button"
          className="btn btn-muted"
          onClick={handleGenerateNotes}
          disabled={generatingNotes}
        >
          {generatingNotes ? "Generating..." : "Generate / Refresh Notes"}
        </button>
      </section>

      {showVideoSection && (
        <section className="card">
          <h3 className="card-title">Video Links</h3>
          <div className="list-block">
            {(content?.videoLinks || []).length ? (
              (content?.videoLinks || []).map((link) => (
                <a
                  key={link}
                  href={link}
                  target="_blank"
                  rel="noreferrer"
                  className="list-item link-item"
                >
                  {link}
                </a>
              ))
            ) : (
              <p className="muted">No video links generated yet.</p>
            )}
          </div>
        </section>
      )}

      <section className="quick-actions">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => navigate(`/quiz?topicId=${topicId}`)}
        >
          Generate Quiz
        </button>
        <button
          type="button"
          className="btn btn-muted"
          onClick={() => navigate(`/doubts?topicId=${topicId}`)}
        >
          Ask Doubt
        </button>
        <Link to="/topics" className="btn btn-muted">
          Back to Topics
        </Link>
      </section>
    </div>
  );
}

export default LearningPage;
