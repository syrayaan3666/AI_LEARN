import { createContext, useContext, useMemo, useState } from "react";

const AppContext = createContext(null);

export function AppProvider({ children }) {
  const [studentId] = useState(1);
  const [activeTopic, setActiveTopic] = useState(null);

  const value = useMemo(
    () => ({
      studentId,
      activeTopic,
      setActiveTopic,
    }),
    [studentId, activeTopic],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useAppContext() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useAppContext must be used within AppProvider");
  }
  return context;
}
