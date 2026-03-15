import { useState } from "react";
import { useAppContext } from "../../context/AppContext.jsx";

const initialState = {
  plannerType: "",

  // Semester planner fields
  skillDomain: "",
  academicLevel: "",
  programDuration: "",
  weeklyHours: "",
  focusArea: "",
  includeCapstone: true,

  // Personal planner fields
  studyDomain: "",
  experienceLevel: "",

  // Shared fields
  careerPath: "",
  learningPace: "",
  duration: "",
};

function CurriculumForm({ onGenerate, loading }) {
  const [form, setForm] = useState(initialState);
  const { studentId } = useAppContext();

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    if (!form.plannerType) {
      alert("Please select a planner type");
      return;
    }

    // Validate SEMESTER fields
    if (form.plannerType === "SEMESTER") {
      if (
        !form.skillDomain ||
        !form.academicLevel ||
        !form.programDuration ||
        !form.weeklyHours ||
        !form.focusArea ||
        !form.careerPath ||
        !form.learningPace
      ) {
        alert("Please fill in all required fields for Semester Planner");
        return;
      }
    }

    // Validate PERSONAL fields
    if (form.plannerType === "PERSONAL") {
      if (
        !form.studyDomain ||
        !form.careerPath ||
        !form.experienceLevel ||
        !form.learningPace ||
        !form.duration
      ) {
        alert("Please fill in all required fields for Personal Planner");
        return;
      }
    }

    const payload =
      form.plannerType === "SEMESTER"
        ? {
            studentId,
            plannerType: "SEMESTER",
            skillDomain: form.skillDomain,
            academicLevel: form.academicLevel,
            programDuration: Number(form.programDuration),
            weeklyHours: Number(form.weeklyHours),
            focusArea: form.focusArea,
            careerPath: form.careerPath,
            learningPace: form.learningPace,
            includeCapstone: Boolean(form.includeCapstone),
          }
        : {
            studentId,
            plannerType: "PERSONAL",
            studyDomain: form.studyDomain,
            careerPath: form.careerPath,
            experienceLevel: form.experienceLevel,
            learningPace: form.learningPace,
            weeklyHours: Number(form.weeklyHours),
            duration: Number(form.duration),
          };

    onGenerate(payload);
  }

  return (
    <form className="card grid" onSubmit={handleSubmit}>
      <h3 className="card-title">Generate Curriculum</h3>

      <label>
        <span>Planner Type</span>
        <select
          className="select"
          value={form.plannerType}
          onChange={(event) => updateField("plannerType", event.target.value)}
          required
        >
          <option value="" disabled>
            Select Planner Type
          </option>
          <option value="SEMESTER">Semester Planner</option>
          <option value="PERSONAL">Personal Planner</option>
        </select>
      </label>

      {form.plannerType === "SEMESTER" ? (
        <>
          <label>
            <span>Select Skill Domain</span>
            <select
              className="select"
              value={form.skillDomain}
              onChange={(event) =>
                updateField("skillDomain", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Skill Domain
              </option>
              <option>AI</option>
              <option>Data Science</option>
              <option>Cybersecurity</option>
              <option>Software Engineering</option>
            </select>
          </label>

          <label>
            <span>Select Academic Level</span>
            <select
              className="select"
              value={form.academicLevel}
              onChange={(event) =>
                updateField("academicLevel", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Academic Level
              </option>
              <option>Bachelors</option>
              <option>Masters</option>
            </select>
          </label>

          <label>
            <span>Select Program Duration</span>
            <select
              className="select"
              value={form.programDuration}
              onChange={(event) =>
                updateField("programDuration", Number(event.target.value))
              }
              required
            >
              <option value="" disabled>
                Select Program Duration (Semesters)
              </option>
              <option value={4}>4</option>
              <option value={6}>6</option>
              <option value={8}>8</option>
            </select>
          </label>

          <label>
            <span>Select Weekly Hours</span>
            <select
              className="select"
              value={form.weeklyHours}
              onChange={(event) =>
                updateField("weeklyHours", Number(event.target.value))
              }
              required
            >
              <option value="" disabled>
                Select Weekly Hours
              </option>
              <option value={15}>15</option>
              <option value={20}>20</option>
              <option value={25}>25</option>
            </select>
          </label>

          <label>
            <span>Select Focus Area</span>
            <select
              className="select"
              value={form.focusArea}
              onChange={(event) => updateField("focusArea", event.target.value)}
              required
            >
              <option value="" disabled>
                Select Focus Area
              </option>
              <option>Research</option>
              <option>Industry</option>
              <option>Startup</option>
            </select>
          </label>

          <label>
            <span>Select Career Path</span>
            <select
              className="select"
              value={form.careerPath}
              onChange={(event) =>
                updateField("careerPath", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Career Path
              </option>
              <option>Job Ready</option>
              <option>Research</option>
              <option>Startup</option>
              <option>Freelance</option>
            </select>
          </label>

          <label>
            <span>Select Learning Pace</span>
            <select
              className="select"
              value={form.learningPace}
              onChange={(event) =>
                updateField("learningPace", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Learning Pace
              </option>
              <option>Fast</option>
              <option>Moderate</option>
              <option>Slow</option>
            </select>
          </label>

          <label
            style={{
              gridColumn: "1 / -1",
              display: "flex",
              alignItems: "center",
              gap: "8px",
            }}
          >
            <input
              type="checkbox"
              checked={Boolean(form.includeCapstone)}
              onChange={(event) =>
                updateField("includeCapstone", event.target.checked)
              }
            />
            <span>Include Capstone Project in Final Semester</span>
          </label>
        </>
      ) : (
        <>
          <label>
            <span>Select Study Domain</span>
            <select
              className="select"
              value={form.studyDomain}
              onChange={(event) =>
                updateField("studyDomain", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Study Domain
              </option>
              <option>AI</option>
              <option>Data Science</option>
              <option>Cybersecurity</option>
              <option>Software Engineering</option>
            </select>
          </label>

          <label>
            <span>Select Career Path</span>
            <select
              className="select"
              value={form.careerPath}
              onChange={(event) =>
                updateField("careerPath", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Career Path
              </option>
              <option>Job Ready</option>
              <option>Research</option>
              <option>Startup</option>
              <option>Freelance</option>
            </select>
          </label>

          <label>
            <span>Select Experience Level</span>
            <select
              className="select"
              value={form.experienceLevel}
              onChange={(event) =>
                updateField("experienceLevel", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Experience Level
              </option>
              <option>Beginner</option>
              <option>Intermediate</option>
              <option>Advanced</option>
            </select>
          </label>

          <label>
            <span>Select Learning Pace</span>
            <select
              className="select"
              value={form.learningPace}
              onChange={(event) =>
                updateField("learningPace", event.target.value)
              }
              required
            >
              <option value="" disabled>
                Select Learning Pace
              </option>
              <option>Fast</option>
              <option>Moderate</option>
              <option>Slow</option>
            </select>
          </label>

          <label>
            <span>Select Weekly Hours</span>
            <select
              className="select"
              value={form.weeklyHours}
              onChange={(event) =>
                updateField("weeklyHours", Number(event.target.value))
              }
              required
            >
              <option value="" disabled>
                Select Weekly Hours
              </option>
              <option value={10}>10</option>
              <option value={15}>15</option>
              <option value={20}>20</option>
              <option value={25}>25</option>
            </select>
          </label>

          <label>
            <span>Select Duration</span>
            <select
              className="select"
              value={form.duration}
              onChange={(event) =>
                updateField("duration", Number(event.target.value))
              }
              required
            >
              <option value="" disabled>
                Select Duration
              </option>
              <option value={3}>3 Months</option>
              <option value={6}>6 Months</option>
              <option value={9}>9 Months</option>
              <option value={12}>12 Months</option>
            </select>
          </label>
        </>
      )}

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? "Generating..." : "Generate Curriculum"}
      </button>
    </form>
  );
}

export default CurriculumForm;
