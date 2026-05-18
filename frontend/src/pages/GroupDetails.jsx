import { useEffect, useState } from "react";
import { Calculator, ReceiptText, RefreshCw, UserPlus } from "lucide-react";
import { expenseApi } from "../api/expenseApi";
import { groupApi } from "../api/groupApi";
import ErrorMessage from "../components/ErrorMessage";
import Loading from "../components/Loading";

const currency = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

export default function GroupDetails({ groupId }) {
  const [group, setGroup] = useState(null);
  const [expenses, setExpenses] = useState([]);
  const [memberId, setMemberId] = useState("");
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadGroup();
  }, [groupId]);

  async function loadGroup() {
    setLoading(true);
    setError("");

    try {
      const [groupData, expenseData] = await Promise.all([
        loadGroupDetails(),
        expenseApi.getGroupExpenses(groupId),
      ]);
      setGroup(groupData);
      setExpenses(expenseData || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

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

  async function addMember(event) {
    event.preventDefault();
    setAdding(true);
    setError("");

    try {
      const updatedGroup = await groupApi.addMember(groupId, memberId);
      setGroup(updatedGroup);
      setMemberId("");
    } catch (err) {
      setError(err.message);
    } finally {
      setAdding(false);
    }
  }

  if (loading) return <Loading label="Loading group ledger" />;

  return (
    <section className="page-stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Group ledger</p>
          <h1>{group?.name || "Group"}</h1>
          <p className="muted">{group?.description || "Expenses and balances for this group."}</p>
        </div>
        <div className="actions">
          <a className="button ghost" href={`#/groups/${groupId}/settlements`}>
            <Calculator size={18} />
            Settle
          </a>
          <a className="button" href={`#/groups/${groupId}/expense`}>
            <ReceiptText size={18} />
            Add expense
          </a>
        </div>
      </div>

      <ErrorMessage message={error} />

      <div className="two-column reverse-mobile">
        <section className="panel">
          <div className="panel-heading">
            <h2>Expenses</h2>
            <button className="text-button" type="button" onClick={loadGroup}>
              <RefreshCw size={16} />
              Refresh
            </button>
          </div>

          {expenses.length ? (
            <div className="expense-list">
              {expenses.map((expense) => (
                <article className="expense-item" key={expense.id}>
                  <div>
                    <h3>{expense.description}</h3>
                    <p>
                      Paid by {expense.paidBy} | {expense.splitType}
                    </p>
                  </div>
                  <strong>{currency.format(Number(expense.amount || 0))}</strong>
                  <div className="split-chips">
                    {expense.splits?.map((split) => (
                      <span className={split.isSettled ? "chip settled" : "chip"} key={`${expense.id}-${split.userId}`}>
                        {split.userName}: {currency.format(Number(split.amountOwed || 0))}
                      </span>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <div className="empty-state">No expenses in this group yet.</div>
          )}
        </section>

        <aside className="side-stack">
          <section className="panel">
            <div className="panel-heading">
              <h2>Members</h2>
              <span className="count-pill">{group?.members?.length || 0}</span>
            </div>
            <div className="member-list">
              {group?.members?.map((member) => (
                <div className="member-row" key={member.userId}>
                  <span className="avatar">{member.name?.charAt(0)?.toUpperCase()}</span>
                  <div>
                    <strong>{member.name}</strong>
                    <small>{member.email}</small>
                  </div>
                </div>
              ))}
            </div>
          </section>

          <form className="panel form-panel" onSubmit={addMember}>
            <h2>Add member</h2>
            <p className="muted">Enter the existing user ID from your backend.</p>
            <input
              value={memberId}
              onChange={(event) => setMemberId(event.target.value)}
              type="number"
              min="1"
              placeholder="User ID"
              required
            />
            <button className="button full" type="submit" disabled={adding}>
              <UserPlus size={18} />
              {adding ? "Adding" : "Add member"}
            </button>
          </form>
        </aside>
      </div>
    </section>
  );
}
