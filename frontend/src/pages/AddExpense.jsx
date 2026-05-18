import { useEffect, useMemo, useState } from "react";
import { ArrowLeft, ReceiptText } from "lucide-react";
import { expenseApi } from "../api/expenseApi";
import { groupApi } from "../api/groupApi";
import ErrorMessage from "../components/ErrorMessage";
import Loading from "../components/Loading";

export default function AddExpense({ groupId }) {
  const [group, setGroup] = useState(null);
  const [form, setForm] = useState({
    description: "",
    amount: "",
    splitType: "EQUAL",
  });
  const [splits, setSplits] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadGroup() {
      setLoading(true);
      setError("");
      try {
        let groupData;
        try {
          groupData = await groupApi.getGroup(groupId);
        } catch {
          const groups = await groupApi.getMyGroups();
          groupData = groups?.find((item) => String(item.id) === String(groupId));
        }
        setGroup(groupData);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadGroup();
  }, [groupId]);

  const totalSplit = useMemo(
    () =>
      Object.values(splits).reduce(
        (sum, value) => sum + Number(value || 0),
        0
      ),
    [splits]
  );

  function updateForm(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }));
  }

  function updateSplit(userId, value) {
    setSplits((current) => ({
      ...current,
      [userId]: value,
    }));
  }

  function buildPayload() {
    const payload = {
      groupId: Number(groupId),
      description: form.description,
      amount: Number(form.amount),
      splitType: form.splitType,
    };

    if (form.splitType !== "EQUAL") {
      payload.splits = (group?.members || []).map((member) => ({
        userId: member.userId,
        value: Number(splits[member.userId] || 0),
      }));
    }

    return payload;
  }

  async function submitExpense(event) {
    event.preventDefault();
    setSaving(true);
    setError("");

    try {
      await expenseApi.addExpense(buildPayload());
      window.location.hash = `#/groups/${groupId}`;
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <Loading label="Preparing expense form" />;

  return (
    <section className="page-stack narrow-page">
      <a className="back-link" href={`#/groups/${groupId}`}>
        <ArrowLeft size={17} />
        Back to group
      </a>

      <div className="page-heading">
        <div>
          <p className="eyebrow">New expense</p>
          <h1>{group?.name || "Group"} expense</h1>
          <p className="muted">Choose a split type and record who owes what.</p>
        </div>
      </div>

      <ErrorMessage message={error} />

      <form className="panel form-grid" onSubmit={submitExpense}>
        <label className="field">
          <span>Description</span>
          <input
            name="description"
            value={form.description}
            onChange={updateForm}
            placeholder="Dinner, cab, tickets"
            required
          />
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
            placeholder="1200"
            required
          />
        </label>

        <label className="field">
          <span>Split type</span>
          <select name="splitType" value={form.splitType} onChange={updateForm}>
            <option value="EQUAL">Equal</option>
            <option value="EXACT">Exact amount</option>
            <option value="PERCENTAGE">Percentage</option>
          </select>
        </label>

        {form.splitType !== "EQUAL" && (
          <div className="split-editor">
            <div className="panel-heading">
              <h2>
                {form.splitType === "EXACT" ? "Exact amounts" : "Percentages"}
              </h2>
              <span className="count-pill">
                Total {form.splitType === "EXACT" ? totalSplit : `${totalSplit}%`}
              </span>
            </div>
            {group?.members?.map((member) => (
              <label className="split-input" key={member.userId}>
                <span>{member.name}</span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={splits[member.userId] || ""}
                  onChange={(event) =>
                    updateSplit(member.userId, event.target.value)
                  }
                  placeholder={form.splitType === "EXACT" ? "Amount" : "Percent"}
                />
              </label>
            ))}
          </div>
        )}

        <button className="button full" type="submit" disabled={saving}>
          <ReceiptText size={18} />
          {saving ? "Saving" : "Save expense"}
        </button>
      </form>
    </section>
  );
}
