function CurriculumViewer({ curriculum }) {
  if (!curriculum) {
    return (
      <section
        className="card"
        style={{ maxHeight: "70vh", overflowY: "auto" }}
      >
        <p className="muted">currently nothing here</p>
      </section>
    );
  }

  const semesters = Array.isArray(curriculum.semesters)
    ? curriculum.semesters
    : [];
  const roadmap = Array.isArray(curriculum.roadmap) ? curriculum.roadmap : [];

  return (
    <section className="card" style={{ maxHeight: "70vh", overflowY: "auto" }}>
      <h3 className="card-title">
        {curriculum.program_title || "Structured Curriculum"}
      </h3>
      {curriculum.summary && <p className="muted">{curriculum.summary}</p>}

      {semesters.length ? (
        semesters.map((sem, si) => (
          <div key={si} className="curriculum-module">
            <h4>Semester {sem.semester}</h4>
            {Array.isArray(sem.courses) && sem.courses.length ? (
              sem.courses.map((course, ci) => (
                <article
                  key={ci}
                  style={{
                    marginBottom: "1rem",
                    paddingLeft: "1rem",
                    borderLeft: "3px solid var(--accent, #4f8ef7)",
                  }}
                >
                  <h5 style={{ margin: "0.4rem 0" }}>
                    {course.title || "currently nothing here"}
                    {course.difficulty && (
                      <span
                        className="muted"
                        style={{ fontWeight: "normal", marginLeft: "0.5rem" }}
                      >
                        — {course.difficulty}
                      </span>
                    )}
                  </h5>

                  {Array.isArray(course.skills) && course.skills.length > 0 && (
                    <p className="muted" style={{ margin: "0.2rem 0" }}>
                      <strong>Skills:</strong> {course.skills.join(", ")}
                    </p>
                  )}

                  {Array.isArray(course.topics) && course.topics.length > 0 && (
                    <ul style={{ margin: "0.2rem 0" }}>
                      {course.topics.map((t, ti) => (
                        <li key={ti}>
                          {t.video_url ? (
                            <a
                              href={t.video_url}
                              target="_blank"
                              rel="noreferrer"
                            >
                              {t.name}
                            </a>
                          ) : (
                            t.name || "currently nothing here"
                          )}
                        </li>
                      ))}
                    </ul>
                  )}

                  {course.outcome_project && (
                    <p className="muted" style={{ margin: "0.2rem 0" }}>
                      <strong>Project:</strong> {course.outcome_project}
                    </p>
                  )}
                </article>
              ))
            ) : (
              <p className="muted">currently nothing here</p>
            )}
          </div>
        ))
      ) : roadmap.length ? (
        roadmap.map((phase, pi) => (
          <div key={pi} className="curriculum-module">
            <h4>{phase.phase || `Phase ${pi + 1}`}</h4>
            {(phase.weeks || phase.duration_weeks) && (
              <p className="muted" style={{ margin: "0.2rem 0 0.6rem" }}>
                {phase.weeks || ""}
                {phase.duration_weeks ? ` (${phase.duration_weeks} weeks)` : ""}
              </p>
            )}

            {Array.isArray(phase.milestones) && phase.milestones.length ? (
              phase.milestones.map((milestone, mi) => (
                <article
                  key={mi}
                  style={{
                    marginBottom: "1rem",
                    paddingLeft: "1rem",
                    borderLeft: "3px solid var(--accent, #4f8ef7)",
                  }}
                >
                  <h5 style={{ margin: "0.4rem 0" }}>
                    {milestone.title || "currently nothing here"}
                  </h5>

                  {(milestone.timeline_weeks ||
                    milestone.estimated_total_hours) && (
                    <p className="muted" style={{ margin: "0.2rem 0" }}>
                      {milestone.timeline_weeks || ""}
                      {milestone.estimated_total_hours
                        ? ` • ${milestone.estimated_total_hours} hours`
                        : ""}
                    </p>
                  )}

                  {Array.isArray(milestone.skills) &&
                    milestone.skills.length > 0 && (
                      <p className="muted" style={{ margin: "0.2rem 0" }}>
                        <strong>Skills:</strong> {milestone.skills.join(", ")}
                      </p>
                    )}

                  {Array.isArray(milestone.topics) &&
                  milestone.topics.length > 0 ? (
                    <ul style={{ margin: "0.2rem 0" }}>
                      {milestone.topics.map((topic, ti) => {
                        const topicName =
                          typeof topic === "string"
                            ? topic
                            : topic?.name || "currently nothing here";
                        const videoUrl =
                          typeof topic === "object" ? topic?.video_url : "";
                        return (
                          <li key={ti}>
                            {videoUrl ? (
                              <a
                                href={videoUrl}
                                target="_blank"
                                rel="noreferrer"
                              >
                                {topicName}
                              </a>
                            ) : (
                              topicName
                            )}
                          </li>
                        );
                      })}
                    </ul>
                  ) : (
                    <p className="muted">currently nothing here</p>
                  )}

                  {milestone.certification &&
                    typeof milestone.certification === "object" && (
                      <p className="muted" style={{ margin: "0.2rem 0" }}>
                        <strong>Certification:</strong>{" "}
                        {milestone.certification.name ||
                          "currently nothing here"}
                        {milestone.certification.provider
                          ? ` (${milestone.certification.provider})`
                          : ""}
                      </p>
                    )}
                </article>
              ))
            ) : (
              <p className="muted">currently nothing here</p>
            )}
          </div>
        ))
      ) : (
        <p className="muted">currently nothing here</p>
      )}
    </section>
  );
}

export default CurriculumViewer;
