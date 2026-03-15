function ChatWindow({ messages }) {
  return (
    <section className="card chat-window">
      {messages.length === 0 ? (
        <p className="muted">currently nothing here</p>
      ) : (
        messages.map((message) => (
          <div
            key={message.id}
            className={`chat-message ${
              message.role === "student"
                ? "chat-message-student"
                : "chat-message-ai"
            }`}
          >
            <strong>{message.role === "student" ? "You" : "AI Tutor"}</strong>
            <p>{message.text}</p>
          </div>
        ))
      )}
    </section>
  );
}

export default ChatWindow;
