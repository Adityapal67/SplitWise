import { useMemo, useState } from "react";
import { ArrowRight, LockKeyhole, Mail, UserRound } from "lucide-react";
import { authApi } from "../api/authApi";
import ErrorMessage from "../components/ErrorMessage";

export default function AuthPage({ mode = "login", onAuth }) {
  const isRegister = mode === "register";
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const copy = useMemo(
    () =>
      isRegister
        ? {
            title: "Create your account",
            subtitle: "Start tracking shared expenses with a cleaner ledger.",
            action: "Create account",
            swap: "Already have an account?",
            link: "Login",
            href: "#/login",
          }
        : {
            title: "Welcome back",
            subtitle: "Open your groups, balances, and settlements in one calm view.",
            action: "Login",
            swap: "New here?",
            link: "Create account",
            href: "#/register",
          },
    [isRegister]
  );

  function updateField(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const payload = isRegister
        ? form
        : { email: form.email, password: form.password };
      const response = isRegister
        ? await authApi.register(payload)
        : await authApi.login(payload);
      onAuth(response);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="auth-grid">
      <div className="auth-hero">
        <div className="hero-orbit">
          <div className="hero-node hero-node-main">LF</div>
          <div className="hero-node small top">Pay</div>
          <div className="hero-node small right">Split</div>
          <div className="hero-node small bottom">Settle</div>
        </div>
        <p className="eyebrow">Shared money, clearly tracked</p>
        <h1>LedgerFlow</h1>
        <p>
          A focused workspace for groups, expenses, balances, and settlement
          flows powered by your Spring Boot API.
        </p>
      </div>

      <form className="auth-card" onSubmit={handleSubmit}>
        <div>
          <p className="eyebrow">Secure access</p>
          <h2>{copy.title}</h2>
          <p className="muted">{copy.subtitle}</p>
        </div>

        <ErrorMessage message={error} />

        {isRegister && (
          <label className="field">
            <span>Name</span>
            <div className="input-shell">
              <UserRound size={18} />
              <input
                name="name"
                value={form.name}
                onChange={updateField}
                placeholder="Aditya Pal"
                required
              />
            </div>
          </label>
        )}

        <label className="field">
          <span>Email</span>
          <div className="input-shell">
            <Mail size={18} />
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={updateField}
              placeholder="you@example.com"
              required
            />
          </div>
        </label>

        <label className="field">
          <span>Password</span>
          <div className="input-shell">
            <LockKeyhole size={18} />
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={updateField}
              placeholder="Your password"
              required
            />
          </div>
        </label>

        <button className="button full" type="submit" disabled={loading}>
          <span>{loading ? "Please wait" : copy.action}</span>
          <ArrowRight size={18} />
        </button>

        <p className="form-swap">
          {copy.swap} <a href={copy.href}>{copy.link}</a>
        </p>
      </form>
    </section>
  );
}
