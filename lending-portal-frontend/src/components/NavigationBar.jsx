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
  <nav className="flex flex-wrap gap-3 mb-6">
    <button
      className={`px-4 py-2 rounded-md font-semibold ${view === "dashboard" ? "bg-sky-600 text-white" : "bg-slate-100 text-slate-700"}`}
      onClick={() => onChange("dashboard")}
    >
      Equipment
    </button>

    <button
      className={`px-4 py-2 rounded-md font-semibold ${view === "requests" ? "bg-sky-600 text-white" : "bg-slate-100 text-slate-700"}`}
      onClick={() => onChange("requests")}
    >
      Borrow Requests
    </button>

    {role === "ADMIN" && (
      <button
        className={`px-4 py-2 rounded-md font-semibold ${view === "manage" ? "bg-sky-600 text-white" : "bg-slate-100 text-slate-700"}`}
        onClick={() => onChange("manage")}
      >
        Manage Equipment
      </button>
    )}

    <div className="ml-auto">
      <button className="px-3 py-2 rounded-md text-sm bg-white border border-slate-200 text-slate-700" onClick={onLogout}>
        Sign out
      </button>
    </div>
  </nav>
);

export default NavigationBar;
