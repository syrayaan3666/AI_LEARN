import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import TopicCard from "../components/Topics/TopicCard.jsx";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import { getTopics } from "../services/topicService.js";

function TopicsPage() {
  const navigate = useNavigate();
  const { setActiveTopic } = useAppContext();
  const [topics, setTopics] = useState([]);
  const [collapsedGroups, setCollapsedGroups] = useState({});
  const [loading, setLoading] = useState(true);

  const groupedTopics = topics.reduce((groups, topic) => {
    const groupName = topic.curriculumName || "Ungrouped Curriculum";
    if (!groups[groupName]) {
      groups[groupName] = [];
    }
    groups[groupName].push(topic);
    return groups;
  }, {});

  const orderedGroups = Object.entries(groupedTopics).sort(([left], [right]) =>
    left.localeCompare(right),
  );

  function toggleGroup(groupName) {
    setCollapsedGroups((current) => ({
      ...current,
      [groupName]: !current[groupName],
    }));
  }

  useEffect(() => {
    async function loadTopics() {
      setLoading(true);
      try {
        const data = await getTopics();
        setTopics(Array.isArray(data) ? data : []);
      } catch {
        setTopics([]);
      } finally {
        setLoading(false);
      }
    }
    loadTopics();
  }, []);

  if (loading) {
    return <LoadingSpinner label="Loading topics" />;
  }

  return (
    <div className="page">
      <h2 className="page-title">Available Topics</h2>
      {orderedGroups.length ? (
        <section
          style={{ display: "flex", flexDirection: "column", gap: "1rem" }}
        >
          {orderedGroups.map(([groupName, groupTopics]) => {
            const collapsed = Boolean(collapsedGroups[groupName]);
            return (
              <section key={groupName} className="card">
                <button
                  type="button"
                  onClick={() => toggleGroup(groupName)}
                  style={{
                    width: "100%",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    background: "transparent",
                    border: 0,
                    padding: 0,
                    cursor: "pointer",
                  }}
                >
                  <div>
                    <h3 style={{ margin: 0 }}>{groupName}</h3>
                    <p className="muted" style={{ margin: "0.35rem 0 0" }}>
                      {groupTopics.length} topic
                      {groupTopics.length !== 1 ? "s" : ""}
                    </p>
                  </div>
                  <span className="muted">
                    {collapsed ? "Expand" : "Minimize"}
                  </span>
                </button>

                {!collapsed && (
                  <div
                    className="grid grid-cols-2"
                    style={{ marginTop: "1rem" }}
                  >
                    {groupTopics.map((topic) => (
                      <TopicCard
                        key={topic.id}
                        topic={topic}
                        onLearn={() => {
                          setActiveTopic(topic);
                          navigate(`/learning/${topic.id}`);
                        }}
                        onGenerateQuiz={() => {
                          setActiveTopic(topic);
                          navigate(`/quiz?topicId=${topic.id}`);
                        }}
                        onAskDoubt={() => {
                          setActiveTopic(topic);
                          navigate(`/doubts?topicId=${topic.id}`);
                        }}
                      />
                    ))}
                  </div>
                )}
              </section>
            );
          })}
        </section>
      ) : (
        <p className="muted">currently nothing here</p>
      )}
    </div>
  );
}

export default TopicsPage;
