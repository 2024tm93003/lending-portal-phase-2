const NavigationBar = ({ view, onChange, role, onLogout }) => (
  <div className="navbarish">
    <button className={view === "dashboard" ? "" : "inactive"} onClick={() => onChange("dashboard")}>
      Equipment
    </button>
    <button className={view === "requests" ? "" : "inactive"} onClick={() => onChange("requests")}>
      Borrow Requests
    </button>
    {role === "ADMIN" && (
      <button className={view === "manage" ? "" : "inactive"} onClick={() => onChange("manage")}>
        Manage Equipment
      </button>
    )}
    <button className="inactive" onClick={onLogout}>
      Sign out
    </button>
  </div>
);

export default NavigationBar;
