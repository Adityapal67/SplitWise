import { useEffect, useState } from "react";
import { ArrowLeft, BadgeCheck, HandCoins } from "lucide-react";
import { groupApi } from "../api/groupApi";
import { settlementApi } from "../api/settlementApi";
import ErrorMessage from "../components/ErrorMessage";
import Loading from "../components/Loading";

const currency = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

export default function Settlements({ groupId }) {
  const [group, setGroup] = useState(null);
  const [debts, setDebts] = useState([]);
  const [history, setHistory] = useState([]);
  const [form, setForm] = useState({ paidToUserId: "", amount: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadSettlements();
  }, [groupId]);

  async function loadGroupDetails() {
    try {
      return await groupApi.getGroup(groupId);
    } catch {
      const groups = await groupApi.getMyGroups();
      const found = groups?.find((item) => String(item.id) === String(groupId));
      if (!found) throw new Error("Group not found");
      return found;
    }
  }

  async function loadSettlements() {
    setLoading(true);
    setError("");
    try {
      const [groupData, debtData, historyData] = await Promise.all([
        loadGroupDetails(),
        settlementApi.getSimplifiedDebts(groupId),
        settlementApi.getHistory(groupId),
      ]);
      setGroup(groupData);
      setDebts(debtData || []);
      setHistory(historyData || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function updateForm(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }));
  }

  async function submitSettlement(event) {
    event.preventDefault();
    setSaving(true);
    setError("");

    try {
      await settlementApi.settle({
        groupId: Number(groupId),
        paidToUserId: Number(form.paidToUserId),
        amount: Number(form.amount),
      });
      setForm({ paidToUserId: "", amount: "" });
      await loadSettlements();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <Loading label="Calculating settlements" />;

  return (
    <section className="page-stack">
      <a className="back-link" href={`#/groups/${groupId}`}>
        <ArrowLeft size={17} />
        Back to group
      </a>

      <div className="page-heading">
        <div>
          <p className="eyebrow">Settlements</p>
          <h1>{group?.name || "Group"} settlement desk</h1>
          <p className="muted">Simplified debts, payment recording, and history.</p>
        </div>
      </div>

      <ErrorMessage message={error} />

      <div className="two-column">
        <section className="panel">
          <div className="panel-heading">
            <h2>Suggested payments</h2>
            <span className="count-pill">{debts.length}</span>
          </div>

          {debts.length ? (
            <div className="settlement-list">
              {debts.map((debt, index) => (
                <div className="settlement-row" key={`${debt.fromUser}-${debt.toUser}-${index}`}>
                  <span>{debt.fromUser}</span>
                  <span className="arrow-line">to</span>
                  <span>{debt.toUser}</span>
                  <strong>{currency.format(Number(debt.amount || 0))}</strong>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">No settlement needed right now.</div>
          )}
        </section>

        <form className="panel form-panel" onSubmit={submitSettlement}>
          <h2>Record payment</h2>
          <p className="muted">Select who you paid and the amount.</p>

          <label className="field">
            <span>Paid to</span>
            <select
              name="paidToUserId"
              value={form.paidToUserId}
              onChange={updateForm}
              required
            >
              <option value="">Choose member</option>
              {group?.members?.map((member) => (
                <option value={member.userId} key={member.userId}>
                  {member.name}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Amount</span>
            <input
              name="amount"
              value={form.amount}
              onChange={updateForm}
              type="number"
              min="0.01"
              step="0.01"
              placeholder="500"
              required
            />
          </label>

          <button className="button full" type="submit" disabled={saving}>
            <HandCoins size={18} />
            {saving ? "Recording" : "Record settlement"}
          </button>
        </form>
      </div>

      <section className="panel">
        <div className="panel-heading">
          <h2>History</h2>
          <BadgeCheck size={18} />
        </div>
        {history.length ? (
          <div className="compact-list">
            {history.map((item) => (
              <div className="compact-row" key={item.id}>
                <span>
                  {item.paidBy} paid {item.paidTo}
                </span>
                <strong>{currency.format(Number(item.amount || 0))}</strong>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">No settlement history yet.</div>
        )}
      </section>
    </section>
  );
}
