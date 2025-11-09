/**
 * NavigationBar
 *
 * Simple navigation component used to switch main views in the application.
 *
 * Props:
 * @param {string} view - Current view key (e.g. 'dashboard', 'requests', 'manage').
 * @param {function(string):void} onChange - Called with the selected view when a nav button is clicked.
 * @param {string} role - Current user's role; controls visibility of admin actions.
 * @param {function():void} onLogout - Logout callback.
 */
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
