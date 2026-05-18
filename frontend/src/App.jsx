import { useEffect, useMemo, useState } from "react";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";
import { clearSession, getStoredUser, getToken, setSession } from "./api/apiClient";
import AuthPage from "./pages/AuthPage";
import Dashboard from "./pages/Dashboard";
import Groups from "./pages/Groups";
import GroupDetails from "./pages/GroupDetails";
import AddExpense from "./pages/AddExpense";
import Settlements from "./pages/Settlements";

function getRoute() {
  return window.location.hash.replace("#", "") || "/dashboard";
}

function navigate(path) {
  window.location.hash = path;
}

export default function App() {
  const [route, setRoute] = useState(getRoute);
  const [user, setUser] = useState(getStoredUser);
  const isAuthenticated = Boolean(getToken());

  useEffect(() => {
    const handleHashChange = () => setRoute(getRoute());
    window.addEventListener("hashchange", handleHashChange);
    return () => window.removeEventListener("hashchange", handleHashChange);
  }, []);

  const routeParts = useMemo(
    () => route.split("/").filter(Boolean),
    [route]
  );

  function handleAuth(authResponse) {
    setSession(authResponse);
    setUser(getStoredUser());
    navigate("/dashboard");
  }

  function handleLogout() {
    clearSession();
    setUser(null);
    navigate("/login");
  }

  function renderPublicRoute() {
    if (route === "/register") {
      return <AuthPage mode="register" onAuth={handleAuth} />;
    }

    return <AuthPage mode="login" onAuth={handleAuth} />;
  }

  function renderPrivateRoute() {
    if (route === "/groups/new") {
      return <Groups startWithCreate />;
    }

    if (routeParts[0] === "groups" && routeParts[1] && routeParts[2] === "expense") {
      return <AddExpense groupId={routeParts[1]} />;
    }

    if (routeParts[0] === "groups" && routeParts[1] && routeParts[2] === "settlements") {
      return <Settlements groupId={routeParts[1]} />;
    }

    if (routeParts[0] === "groups" && routeParts[1]) {
      return <GroupDetails groupId={routeParts[1]} />;
    }

    if (route === "/groups") {
      return <Groups />;
    }

    return <Dashboard />;
  }

  if (!isAuthenticated && ["/login", "/register"].includes(route)) {
    return <main className="auth-shell">{renderPublicRoute()}</main>;
  }

  return (
    <div className="app-shell">
      {isAuthenticated && (
        <Navbar user={user} route={route} onLogout={handleLogout} />
      )}
      <main className={isAuthenticated ? "page-shell" : "auth-shell"}>
        <ProtectedRoute isAuthenticated={isAuthenticated} onAuth={handleAuth}>
          {renderPrivateRoute()}
        </ProtectedRoute>
      </main>
    </div>
  );
}
