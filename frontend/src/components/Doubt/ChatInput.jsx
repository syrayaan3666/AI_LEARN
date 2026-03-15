import { useState } from "react";

function ChatInput({ onSend, disabled }) {
  const [text, setText] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    if (!text.trim()) {
      return;
    }
    onSend(text.trim());
    setText("");
  }

  return (
    <form className="chat-input" onSubmit={handleSubmit}>
      <input
        className="input"
        placeholder="Ask your doubt..."
        value={text}
        onChange={(event) => setText(event.target.value)}
        disabled={disabled}
      />
      <button type="submit" className="btn btn-primary" disabled={disabled}>
        Send
      </button>
    </form>
  );
}

export default ChatInput;
