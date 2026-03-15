import { useState } from "react";
import CurriculumForm from "../components/Curriculum/CurriculumForm.jsx";
import CurriculumViewer from "../components/Curriculum/CurriculumViewer.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import {
  generateCurriculum,
  generateMockCurriculum,
  refineCurriculum,
  saveAsTopics,
} from "../services/curriculumService.js";

function CurriculumPage() {
  const { studentId } = useAppContext();
  const [formResetKey, setFormResetKey] = useState(0);
  const [loading, setLoading] = useState(false);
  const [curriculum, setCurriculum] = useState(null);
  const [curriculumId, setCurriculumId] = useState(null);
  const [refinePrompt, setRefinePrompt] = useState("");
  const [refining, setRefining] = useState(false);
  const [saving, setSaving] = useState(false);
  const [generatorMessage, setGeneratorMessage] = useState("");

  async function handleGenerate(values) {
    setLoading(true);
    setGeneratorMessage("");
    try {
      const data = await generateCurriculum({ studentId, ...values });
      setCurriculumId(data.curriculumId);
      setCurriculum(data?.curriculum || data);
      setFormResetKey((current) => current + 1);
    } catch (error) {
      setCurriculum(null);
      setCurriculumId(null);
      const errorMsg = error?.message || "Failed to generate curriculum.";
      const isServiceDown =
        errorMsg.includes("unavailable") ||
        errorMsg.includes("404") ||
        errorMsg.includes("Not Found");
      const displayMsg = isServiceDown
        ? "❌ The curriculum generator service is currently unavailable. Please try the Mock Curriculum button for testing, or try again in a few moments."
        : errorMsg;
      setGeneratorMessage(displayMsg);
    } finally {
      setLoading(false);
    }
  }

  async function handleRefine() {
    if (!refinePrompt.trim() || !curriculumId) return;
    setRefining(true);
    try {
      const data = await refineCurriculum({
        studentId,
        curriculumId,
        refinementPrompt: refinePrompt,
      });
      setCurriculumId(data.curriculumId);
      setCurriculum(data?.curriculum || data);
      setRefinePrompt("");
    } catch (error) {
      setGeneratorMessage(error?.message || "Failed to refine curriculum.");
    } finally {
      setRefining(false);
    }
  }

  async function handleLoadMockCurriculum() {
    setLoading(true);
    setGeneratorMessage("");
    try {
      const data = await generateMockCurriculum(studentId);
      setCurriculumId(data.curriculumId);
      setCurriculum(data?.curriculum || data);
      setGeneratorMessage("Mock curriculum loaded for testing.");
    } catch (error) {
      setCurriculum(null);
      setCurriculumId(null);
      setGeneratorMessage(error?.message || "Failed to load mock curriculum.");
    } finally {
      setLoading(false);
    }
  }

  async function handleSaveAsTopics() {
    if (!curriculumId) return;
    const curriculumName = window.prompt("Enter a name for this curriculum");
    if (curriculumName == null) return;
    const trimmedCurriculumName = curriculumName.trim();
    if (!trimmedCurriculumName) {
      setGeneratorMessage("Curriculum name is required before saving topics.");
      return;
    }

    setSaving(true);
    setGeneratorMessage("");
    try {
      const topics = await saveAsTopics(
        curriculumId,
        studentId,
        trimmedCurriculumName,
      );
      setCurriculum(null);
      setCurriculumId(null);
      setRefinePrompt("");
      setFormResetKey((current) => current + 1);
      setGeneratorMessage(
        topics.length
          ? `${topics.length} topic${topics.length !== 1 ? "s" : ""} saved under ${trimmedCurriculumName}. You can generate another curriculum now.`
          : `All topics for ${trimmedCurriculumName} were already saved.`,
      );
    } catch (error) {
      setGeneratorMessage(
        error?.message || "Failed to save topics. Please try again.",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="page">
      <h2 className="page-title">Curriculum Generator</h2>
      <p className="muted">
        Generate a personalised AI curriculum. Topics, Learning, Quiz, and
        Doubts work without one too.
      </p>
      <button
        type="button"
        className="btn btn-muted"
        onClick={handleLoadMockCurriculum}
        disabled={loading}
        style={{ marginBottom: "0.75rem" }}
      >
        Load Mock Curriculum (Testing)
      </button>
      {generatorMessage && (
        <div
          style={{
            padding: "0.75rem",
            marginBottom: "0.75rem",
            borderRadius: "0.25rem",
            backgroundColor: generatorMessage.includes("❌") ? "#fee" : "#eee",
            borderLeft: generatorMessage.includes("❌")
              ? "4px solid #c33"
              : "4px solid #999",
            color: generatorMessage.includes("❌") ? "#c33" : "#666",
          }}
        >
          {generatorMessage}
        </div>
      )}
      <div className="grid grid-cols-2">
        <CurriculumForm
          key={formResetKey}
          onGenerate={handleGenerate}
          loading={loading}
        />
        <CurriculumViewer curriculum={curriculum} />
      </div>

      {curriculum && (
        <div
          style={{
            marginTop: "1.5rem",
            display: "flex",
            flexDirection: "column",
            gap: "1rem",
          }}
        >
          {/* Save as Topics */}
          <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSaveAsTopics}
              disabled={saving}
            >
              {saving ? "Saving…" : "Save as Topics"}
            </button>
          </div>

          {/* Refine chatbox */}
          <div
            className="card"
            style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}
          >
            <h4 style={{ margin: 0 }}>Refine Curriculum</h4>
            <textarea
              className="input"
              rows={3}
              placeholder="Describe what you'd like to change, e.g. 'Add more advanced topics in semester 2'"
              value={refinePrompt}
              onChange={(e) => setRefinePrompt(e.target.value)}
              style={{
                resize: "vertical",
                width: "100%",
                boxSizing: "border-box",
              }}
            />
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleRefine}
              disabled={refining || !refinePrompt.trim()}
              style={{ alignSelf: "flex-start" }}
            >
              {refining ? "Refining…" : "Refine"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default CurriculumPage;
