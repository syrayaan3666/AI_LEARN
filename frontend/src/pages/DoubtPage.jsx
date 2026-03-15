import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import ChatInput from "../components/Doubt/ChatInput.jsx";
import ChatWindow from "../components/Doubt/ChatWindow.jsx";
import LoadingSpinner from "../components/Shared/LoadingSpinner.jsx";
import { useAppContext } from "../context/AppContext.jsx";
import { askDoubt, getDoubtHistory } from "../services/doubtService.js";

function normalizeHistory(data) {
  if (!Array.isArray(data)) {
    return [];
  }

  return data.flatMap((item, index) => {
    const userText = item.studentMessage || item.question || item.doubtText;
    const aiText = item.aiResponse || item.answer;
    return [
      userText
        ? { id: `hist-user-${index}`, role: "student", text: userText }
        : null,
      aiText ? { id: `hist-ai-${index}`, role: "ai", text: aiText } : null,
    ].filter(Boolean);
  });
}

function DoubtPage() {
  const { studentId, activeTopic } = useAppContext();
  const [searchParams] = useSearchParams();
  const topicId = searchParams.get("topicId") || activeTopic?.id || "";

  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    async function loadHistory() {
      setLoading(true);
      try {
        const data = await getDoubtHistory(studentId);
        setMessages(normalizeHistory(data));
      } catch {
        setMessages([]);
      } finally {
        setLoading(false);
      }
    }

    loadHistory();
  }, [studentId]);

  const headerText = useMemo(() => `Current Topic: ${topicId}`, [topicId]);

  async function handleSend(text) {
    const tempUser = { id: `u-${Date.now()}`, role: "student", text };
    setMessages((prev) => [...prev, tempUser]);
    setSending(true);

    try {
      const response = await askDoubt({
        studentId,
        topicId,
        doubtText: text,
      });
      setMessages((prev) => [
        ...prev,
        {
          id: `a-${Date.now()}`,
          role: "ai",
          text:
            response?.answer ||
            response?.aiResponse ||
            "currently nothing here",
        },
      ]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          id: `a-${Date.now()}`,
          role: "ai",
          text: "currently nothing here",
        },
      ]);
    } finally {
      setSending(false);
    }
  }

  if (loading) {
    return <LoadingSpinner label="Loading doubt history" />;
  }

  return (
    <div className="page">
      <h2 className="page-title">AI Doubt Assistant</h2>
      <p className="muted">
        {topicId ? headerText : "Current Topic: currently nothing here"}
      </p>
      <ChatWindow messages={messages} />
      <ChatInput onSend={handleSend} disabled={sending} />
    </div>
  );
}

export default DoubtPage;
