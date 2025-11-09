/**
 * AuthScreen
 *
 * Renders the authentication UI used for login and quick signup flows.
 * Controlled via `credentials` and `onCredentialsChange`.
 *
 * Props:
 * @param {('login'|'signup')} mode - Which form mode to render.
 * @param {function(string):void} onModeChange - Called when the user switches between modes.
 * @param {Object} credentials - Current credential values: { username, password, displayName }.
 * @param {function(Object):void} onCredentialsChange - Called with updated credentials when any field changes.
 * @param {function(Event):void} onSubmit - Submit handler for the form.
 * @param {string} [infoText] - Optional info/status text displayed below the form.
 */
const AuthScreen = ({ mode, onModeChange, credentials, onCredentialsChange, onSubmit, infoText }) => {
  const isSignup = mode === "signup";
  /**
   * Update a specific credential field and notify parent.
   * @param {string} field - Field name to update (e.g. 'username').
   * @returns {function(Event):void}
   */
  const updateField = (field) => (event) => {
    onCredentialsChange({ ...credentials, [field]: event.target.value });
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-slate-50">
      <div className="bg-white shadow rounded-lg w-full max-w-md p-6">
        <h2 className="text-2xl font-semibold mb-1">{isSignup ? "Create an account" : "Welcome back"}</h2>
        <p className="text-sm text-slate-500 mb-4">Use your school credentials to access the lending portal.</p>

        <div className="flex gap-2 mb-4" role="tablist" aria-label="Authentication mode">
          <button
            type="button"
            className={`flex-1 py-2 rounded-md text-sm font-semibold ${mode === "login" ? "bg-sky-600 text-white" : "bg-slate-100 text-slate-700"}`}
            onClick={() => onModeChange("login")}
          >
            Log In
          </button>
          <button
            type="button"
            className={`flex-1 py-2 rounded-md text-sm font-semibold ${mode === "signup" ? "bg-sky-600 text-white" : "bg-slate-100 text-slate-700"}`}
            onClick={() => onModeChange("signup")}
          >
            Quick Signup
          </button>
        </div>

        <form className="space-y-3" onSubmit={onSubmit}>
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-slate-700">
              Username
            </label>
            <input
              id="username"
              value={credentials.username}
              onChange={updateField("username")}
              autoComplete="username"
              required
              className="mt-1 block w-full rounded-md border-slate-200 shadow-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-slate-700">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={credentials.password}
              onChange={updateField("password")}
              autoComplete={isSignup ? "new-password" : "current-password"}
              required
              className="mt-1 block w-full rounded-md border-slate-200 shadow-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          {isSignup && (
            <div>
              <label htmlFor="displayName" className="block text-sm font-medium text-slate-700">
                Display Name
              </label>
              <input
                id="displayName"
                value={credentials.displayName}
                onChange={updateField("displayName")}
                autoComplete="name"
                required
                className="mt-1 block w-full rounded-md border-slate-200 shadow-sm px-3 py-2 focus:outline-none focus:ring-2 focus:ring-sky-500"
              />
            </div>
          )}

          <div>
            <button type="submit" className="w-full bg-sky-600 hover:bg-sky-700 text-white py-2 px-4 rounded-md font-semibold">
              {isSignup ? "Create account" : "Sign in"}
            </button>
          </div>
        </form>

        {infoText && (
          <p className="text-sm text-slate-600 mt-3" role="status">
            {infoText}
          </p>
        )}
      </div>
    </div>
  );
};

export default AuthScreen;
