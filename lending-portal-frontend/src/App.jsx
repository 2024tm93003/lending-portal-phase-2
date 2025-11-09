import { useCallback, useEffect, useMemo, useState } from "react";
import { Navigate, Route, Routes, useNavigate, useParams } from "react-router-dom";
import AuthScreen from "./components/AuthScreen";
import NavigationBar from "./components/NavigationBar";
import EquipmentCatalog from "./components/EquipmentCatalog";
import BorrowRequestForm from "./components/BorrowRequestForm";
import RequestTable from "./components/RequestTable";
import ManageEquipmentForm from "./components/ManageEquipmentForm";

const CONFIG_PATH = "/config/app.properties";
const DEFAULT_VIEW = "dashboard";
const ROUTABLE_VIEWS = ["dashboard", "requests", "manage"];

const stripTrailingSlash = (value) => {
  if (!value) return value;
  return value.endsWith("/") ? value.slice(0, -1) : value;
};
const parseProperties = (rawText) => {
  return rawText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line && !line.startsWith("#"))
    .reduce((acc, line) => {
      const separatorIndex = line.indexOf("=");
      if (separatorIndex === -1) return acc;
      const key = line.slice(0, separatorIndex).trim();
      const value = line.slice(separatorIndex + 1).trim();
      if (key) acc[key] = value;
      return acc;
    }, {});
};

function PortalApp() {
  const { section, subsection } = useParams();
  const navigate = useNavigate();
  const isAuthRoute = section === "auth";
  const authMode = subsection === "signup" ? "signup" : "login";
  const requestedView = ROUTABLE_VIEWS.includes(section) ? section : DEFAULT_VIEW;
  const view = isAuthRoute ? DEFAULT_VIEW : requestedView;
  const [apiRoot, setApiRoot] = useState("");
  const [configLoaded, setConfigLoaded] = useState(false);
  const [configError, setConfigError] = useState("");
  const [token, setToken] = useState(() => window.localStorage.getItem("lend_token") || "");
  const [user, setUser] = useState(null);
  const [loginStuff, setLoginStuff] = useState({ username: "", password: "", displayName: "", confirmPassword: "" });
  const [gearList, setGearList] = useState([]);
  const [reqs, setReqs] = useState([]);
  const [filterBucket, setFilterBucket] = useState({ category: "", availableOnly: false });
  const [borForm, setBorForm] = useState({ equipmentId: "", startDate: "", endDate: "", qty: 1 });
  const [equipForm, setEquipForm] = useState({ itemName: "", category: "", conditionNote: "", totalQuantity: 1 });
  const [equipFormErrors, setEquipFormErrors] = useState({});
  const [borFormErrors, setBorFormErrors] = useState({});
  const [authErrors, setAuthErrors] = useState({});
  const [infoText, setInfoText] = useState("");
  const [inFlightRequests, setInFlightRequests] = useState(0);
  const trackedFetch = useCallback((...args) => {
    setInFlightRequests((count) => count + 1);
    return fetch(...args).finally(() => {
      setInFlightRequests((count) => Math.max(0, count - 1));
    });
  }, []);
  const loadingIndicator =
    inFlightRequests > 0 ? (
      <div className="loadingIndicator" role="status" aria-live="polite">
        <span className="spinner" aria-hidden="true" />
        <span>Loading...</span>
      </div>
    ) : null;
  const switchAuthMode = useCallback(
    (nextMode) => {
      if (nextMode !== "login" && nextMode !== "signup") return;
      const target = `/auth/${nextMode}`;
      if (section === "auth" && subsection === nextMode) return;
      navigate(target, { replace: section === "auth" });
    },
    [navigate, section, subsection]
  );

  useEffect(() => {
    if (section === "auth" && !subsection) {
      navigate("/auth/login", { replace: true });
    }
  }, [section, subsection, navigate]);

  useEffect(() => {
    if (isAuthRoute) return;
    if (!section || section !== requestedView || subsection) {
      navigate(`/${requestedView}`, { replace: true });
    }
  }, [section, subsection, requestedView, navigate, isAuthRoute]);

  useEffect(() => {
    if (!token && !isAuthRoute) {
      navigate("/auth/login", { replace: true });
    }
    if (token && user && isAuthRoute) {
      navigate(`/${requestedView}`, { replace: true });
    }
  }, [token, user, isAuthRoute, navigate, requestedView]);

  useEffect(() => {
    let isMounted = true;
    const loadConfig = async () => {
      try {
        const response = await trackedFetch(CONFIG_PATH, { cache: "no-store" });
        if (!response.ok) {
          throw new Error(`Config file missing (${response.status})`);
        }
        const text = await response.text();
        const props = parseProperties(text);
        const configured = stripTrailingSlash(props.API_ROOT || props.VITE_API_URL || "");
        if (!configured || !/^https?:\/\//i.test(configured)) {
          throw new Error("API_ROOT missing or invalid in app.properties");
        }
        if (isMounted) {
          setApiRoot(configured);
        }
      } catch (error) {
        const fallback = stripTrailingSlash(import.meta.env.VITE_API_URL || "");
        if (fallback && /^https?:\/\//i.test(fallback) && isMounted) {
          console.warn("Using VITE_API_URL fallback because config load failed:", error);
          setApiRoot(fallback);
        } else if (isMounted) {
          setConfigError(error.message || "Could not load API configuration.");
        }
      } finally {
        if (isMounted) {
          setConfigLoaded(true);
        }
      }
    };
    loadConfig();
    return () => {
      isMounted = false;
    };
  }, [trackedFetch]);

  useEffect(() => {
    if (view === "manage" && user && user.role !== "ADMIN") {
      navigate(`/${DEFAULT_VIEW}`, { replace: true });
    }
  }, [view, user, navigate]);

  useEffect(() => {
    if (!token) {
      setUser(null);
      setGearList([]);
      setReqs([]);
      return;
    }
    if (!apiRoot) return;
    trackedFetch(`${apiRoot}/auth/me`, {
      headers: { "X-Auth-Token": token },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Auth failed");
        return res.json();
      })
        .then((data) => {
        setUser(data);
        setLoginStuff({ username: "", password: "", displayName: "", confirmPassword: "" });
      })
      .catch(() => {
        setToken("");
        window.localStorage.removeItem("lend_token");
      });
  }, [token, apiRoot, trackedFetch]);

  useEffect(() => {
    if (!token || !user || !apiRoot) return;
    const params = [];
    if (filterBucket.category) params.push(`category=${encodeURIComponent(filterBucket.category)}`);
    if (filterBucket.availableOnly) params.push("availableOnly=true");
    const query = params.length ? `?${params.join("&")}` : "";
    trackedFetch(`${apiRoot}/equipment${query}`, {
      headers: { "X-Auth-Token": token },
    })
      .then((res) => {
        if (!res.ok) throw new Error("fail list");
        return res.json();
      })
      .then((data) => setGearList(data))
      .catch(() => {
        setInfoText("Couldn't load equipment right now");
      });
  }, [token, user, filterBucket, apiRoot, trackedFetch]);

  useEffect(() => {
    if (!token || !user || !apiRoot) return;
    const mine = user?.role === "STUDENT" ? "?mine=true" : "";
    trackedFetch(`${apiRoot}/requests${mine}`, {
      headers: { "X-Auth-Token": token },
    })
      .then((res) => {
        if (!res.ok) throw new Error("fail reqs");
        return res.json();
      })
      .then((data) => setReqs(data))
      .catch(() => {
        setReqs([]);
      });
  }, [token, user, apiRoot, trackedFetch]);

  const handleLoginSubmit = (evt) => {
    evt.preventDefault();
    // client-side validation
    const errs = {};
    const username = (loginStuff.username || "").trim();
    const password = loginStuff.password || "";
  if (!username) errs.username = "Username is required";
    if (!password) errs.password = "Password is required";
    if (authMode === "signup") {
      // stricter password policy: min 8, at least one uppercase, one lowercase, one digit, one special char
      const pwd = password || "";
      const minLen = 8;
      const hasUpper = /[A-Z]/.test(pwd);
      const hasLower = /[a-z]/.test(pwd);
      const hasDigit = /[0-9]/.test(pwd);
      const hasSpecial = /[^A-Za-z0-9]/.test(pwd);
      if (pwd.length < minLen) errs.password = errs.password || `Password must be at least ${minLen} characters`;
      else if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
        errs.password = errs.password || "Password must include uppercase, lowercase, number, and special character";
      }
      if (!loginStuff.displayName || loginStuff.displayName.trim().length < 2) errs.displayName = "Display name is required";
      if ((loginStuff.confirmPassword || "") !== pwd) errs.confirmPassword = "Passwords do not match";
    }
    if (Object.keys(errs).length) {
      setAuthErrors(errs);
      setInfoText("Please fix the highlighted fields");
      return;
    }
    setAuthErrors({});
    const path = authMode === "signup" ? "signup" : "login";
    const payload = authMode === "signup"
      ? { username: loginStuff.username, password: loginStuff.password, displayName: loginStuff.displayName }
      : { username: loginStuff.username, password: loginStuff.password };
    trackedFetch(`${apiRoot}/auth/${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    })
      .then((res) => {
        if (!res.ok) return res.json().then((d) => Promise.reject(d));
        return res.json();
      })
      .then((data) => {
        setToken(data.token);
        window.localStorage.setItem("lend_token", data.token);
        setUser(data);
        navigate(`/${DEFAULT_VIEW}`);
        setInfoText("Welcome back!");
      })
      .catch((err) => {
        setInfoText(err?.message || "Login/signup failed");
      });
  };

  const logout = () => {
    setToken("");
    setUser(null);
    window.localStorage.removeItem("lend_token");
    setGearList([]);
    setReqs([]);
    navigate("/auth/login");
  };

  const sanitizedGearForBorrow = useMemo(() => {
    if (!gearList?.length) return [];
    return gearList.filter((g) => g.availableQuantity > 0);
  }, [gearList]);

  const submitBorrow = (evt) => {
    evt.preventDefault();
    if (!apiRoot) return;
    const errs = {};
    const equipmentId = borForm.equipmentId;
    const qty = Number(borForm.qty || 0);
    const start = borForm.startDate;
    const end = borForm.endDate;
    if (!equipmentId) errs.equipmentId = "Please choose an item";
    const selected = gearList.find((g) => String(g.id) === String(equipmentId));
    if (!selected) errs.equipmentId = errs.equipmentId || "Selected item not found";
    if (!Number.isFinite(qty) || !Number.isInteger(qty) || qty < 1) errs.qty = "Quantity must be an integer >= 1";
    if (selected && Number.isFinite(qty) && qty > selected.availableQuantity) errs.qty = `Only ${selected.availableQuantity} available`;
    if (!start) errs.startDate = "Start date is required";
    if (!end) errs.endDate = "End date is required";
    if (start && end) {
      const s = new Date(start);
      const e = new Date(end);
      if (isNaN(s.getTime()) || isNaN(e.getTime())) {
        errs.startDate = errs.startDate || "Invalid date";
        errs.endDate = errs.endDate || "Invalid date";
      } else if (s > e) {
        errs.endDate = "End date must be the same or after start date";
      }
    }
    if (Object.keys(errs).length) {
      setBorFormErrors(errs);
      setInfoText("Please fix the highlighted fields");
      return;
    }
    setBorFormErrors({});
    const payload = { ...borForm, quantity: undefined };
    payload.qty = qty;
    payload.equipmentId = Number(equipmentId);
    trackedFetch(`${apiRoot}/requests`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Auth-Token": token,
      },
      body: JSON.stringify(payload),
    })
      .then((res) => {
        if (!res.ok) return res.json().then((d) => Promise.reject(d));
        return res.json();
      })
      .then((req) => {
        setReqs((curr) => [req, ...curr]);
        setInfoText("Request submitted.");
        navigate("/requests");
      })
      .catch((err) => {
        setInfoText(err?.message || "Could not make request");
      });
  };

  const touchRequests = () => {
    if (!apiRoot) return;
    const mine = user?.role === "STUDENT" ? "?mine=true" : "";
    trackedFetch(`${apiRoot}/requests${mine}`, {
      headers: { "X-Auth-Token": token },
    })
      .then((r) => (r.ok ? r.json() : []))
      .then((data) => setReqs(data));
  };

  const handleViewChange = (nextView) => {
    if (!ROUTABLE_VIEWS.includes(nextView)) return;
    navigate(`/${nextView}`);
  };

  const handleDecision = (id, action, msg) => {
    if (!apiRoot) return;
    trackedFetch(`${apiRoot}/requests/${id}/${action}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Auth-Token": token,
      },
      body: msg ? JSON.stringify({ message: msg }) : null,
    })
      .then((res) => {
        if (!res.ok) return res.json().then((d) => Promise.reject(d));
        return res.json();
      })
      .then(() => {
        touchRequests();
        setInfoText(`Request ${action} ok`);
      })
      .catch((err) => {
        setInfoText(err?.message || `${action} didn't work`);
      });
  };

  const createEquipment = (evt) => {
    evt.preventDefault();
    if (!apiRoot) return;
    // client-side validation
    const errs = {};
    const name = (equipForm.itemName || "").trim();
    const category = (equipForm.category || "").trim();
    const total = Number(equipForm.totalQuantity || 0);
    const available = equipForm.availableQuantity !== undefined && equipForm.availableQuantity !== null && String(equipForm.availableQuantity).trim() !== "" ? Number(equipForm.availableQuantity) : total;
    if (!name) errs.itemName = "Name is required";
    if (!category) errs.category = "Category is required";
    if (!Number.isFinite(total) || !Number.isInteger(total) || total < 1) errs.totalQuantity = "Total must be an integer >= 1";
    if (!Number.isFinite(available) || available < 0) errs.availableQuantity = "Available must be a number >= 0";
    if (Number.isFinite(total) && Number.isFinite(available) && available > total) errs.availableQuantity = "Available cannot exceed total quantity";
    if (Object.keys(errs).length) {
      setEquipFormErrors(errs);
      setInfoText("Please fix the highlighted fields");
      return;
    }
    setEquipFormErrors({});
    const data = {
      ...equipForm,
      totalQuantity: total,
      availableQuantity: available,
    };
    trackedFetch(`${apiRoot}/equipment`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Auth-Token": token,
      },
      body: JSON.stringify(data),
    })
      .then((res) => {
        if (!res.ok) return res.json().then((d) => Promise.reject(d));
        return res.json();
      })
      .then((created) => {
        setGearList((prev) => [created, ...prev]);
        setInfoText("Equipment added");
      })
      .catch((err) => {
        setInfoText(err?.message || "Failed to save equipment");
      });
  };

  if (!apiRoot) {
    const message = configLoaded
      ? configError || "Set API_ROOT in public/config/app.properties."
      : "Reading config/app.properties...";
    const title = configLoaded ? "Configuration issue" : "Loading configuration";
    return (
      <>
        <div className="max-w-5xl mx-auto p-6">
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold">{title}</h2>
            <p className="text-sm text-slate-600">{message}</p>
          </div>
        </div>
        {loadingIndicator}
      </>
    );
  }

  if (!token || !user) {
    return (
      <>
        <AuthScreen
          mode={authMode}
          onModeChange={switchAuthMode}
          credentials={loginStuff}
          onCredentialsChange={setLoginStuff}
          onSubmit={handleLoginSubmit}
          fieldErrors={authErrors}
          infoText={infoText}
        />
        {loadingIndicator}
      </>
    );
  }

  const canSubmitBorrow = user.role === "STUDENT" || user.role === "STAFF";

  return (
    <>
      <div className="max-w-5xl mx-auto p-6">
        <header className="mb-6">
          <h1 className="text-3xl font-bold">Equipment Lending Portal</h1>
          <p className="text-sm text-slate-600 mt-1">Signed in as <span className="font-medium">{user.displayName}</span> <span className="text-slate-400">({user.role})</span></p>
        </header>

        <NavigationBar view={view} onChange={handleViewChange} role={user.role} onLogout={logout} />
        {infoText && <p className="text-sm text-slate-600">{infoText}</p>}

        <main className="space-y-6 mt-4">
          {view === "dashboard" && (
            <>
              <EquipmentCatalog items={gearList} filters={filterBucket} onFiltersChange={setFilterBucket} />
              {canSubmitBorrow && (
                <BorrowRequestForm
                  items={sanitizedGearForBorrow}
                  formState={borForm}
                  onChange={setBorForm}
                  onSubmit={submitBorrow}
                  formErrors={borFormErrors}
                />
              )}
            </>
          )}
          {view === "requests" && <RequestTable requests={reqs} role={user.role} onDecision={handleDecision} />}
          {view === "manage" && user.role === "ADMIN" && (
            <ManageEquipmentForm formState={equipForm} onChange={setEquipForm} onSubmit={createEquipment} formErrors={equipFormErrors} />
          )}
        </main>
      </div>
      {loadingIndicator}
    </>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/:section?/:subsection?" element={<PortalApp />} />
      <Route path="*" element={<Navigate to={`/${DEFAULT_VIEW}`} replace />} />
    </Routes>
  );
}

export default App;
