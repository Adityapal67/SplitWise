import AuthPage from "../pages/AuthPage";

export default function ProtectedRoute({ children, isAuthenticated, onAuth }) {
  if (!isAuthenticated) {
    return <AuthPage mode="login" onAuth={onAuth} />;
  }

  return children;
}
