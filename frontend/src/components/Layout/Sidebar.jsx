import { NavLink } from "react-router-dom";

const menuItems = [
  { label: "Dashboard", path: "/dashboard" },
  { label: "Topics", path: "/topics" },
  { label: "Quiz", path: "/quiz" },
  { label: "Quiz History", path: "/quiz-history" },
  { label: "Doubts", path: "/doubts" },
  { label: "Curriculum Generator", path: "/curriculum" },
];

function Sidebar() {
  return (
    <aside className="sidebar card">
      <h2 className="sidebar-title">AI Learn</h2>
      <p className="muted sidebar-subtitle">Adaptive Learning Platform</p>
      <nav className="sidebar-nav">
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `sidebar-link ${isActive ? "sidebar-link-active" : ""}`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
