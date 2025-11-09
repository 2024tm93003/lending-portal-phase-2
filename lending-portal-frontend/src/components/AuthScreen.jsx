const AuthScreen = ({ mode, onModeChange, credentials, onCredentialsChange, onSubmit, infoText }) => {
  const isSignup = mode === "signup";
  const updateField = (field) => (event) => {
    onCredentialsChange({ ...credentials, [field]: event.target.value });
  };

  return (
    <div className="authModal">
      <div className="cardy authCardSimple">
        <h2>{isSignup ? "Create an account" : "Welcome back"}</h2>
        <p className="tiny">Use your school credentials to access the lending portal.</p>

        <div className="authTabs" role="tablist" aria-label="Authentication mode">
          <button type="button" className={mode === "login" ? "active" : ""} onClick={() => onModeChange("login")}>
            Log In
          </button>
          <button type="button" className={mode === "signup" ? "active" : ""} onClick={() => onModeChange("signup")}>
            Quick Signup
          </button>
        </div>

        <form className="authForm" onSubmit={onSubmit}>
          <label htmlFor="username">Username</label>
          <input id="username" value={credentials.username} onChange={updateField("username")} autoComplete="username" required />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={credentials.password}
            onChange={updateField("password")}
            autoComplete={isSignup ? "new-password" : "current-password"}
            required
          />

          {isSignup && (
            <>
              <label htmlFor="displayName">Display Name</label>
                <input
                  id="displayName"
                  value={credentials.displayName}
                  onChange={updateField("displayName")}
                  autoComplete="name"
                  required
                />
            </>
          )}

          <button type="submit" className="btn btnPrimary btnFull">
            {isSignup ? "Create account" : "Sign in"}
          </button>
        </form>

        {infoText && (
          <p className="tiny" role="status">
            {infoText}
          </p>
        )}
      </div>
    </div>
  );
};

export default AuthScreen;
