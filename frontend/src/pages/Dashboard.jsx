import { useEffect, useState } from "react";
import { ArrowDownLeft, ArrowUpRight, Scale, UsersRound } from "lucide-react";
import { expenseApi } from "../api/expenseApi";
import { groupApi } from "../api/groupApi";
import ErrorMessage from "../components/ErrorMessage";
import Loading from "../components/Loading";

const currency = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

export default function Dashboard() {
  const [balances, setBalances] = useState(null);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const [balanceData, groupData] = await Promise.all([
          expenseApi.getMyBalances(),
          groupApi.getMyGroups(),
        ]);
        setBalances(balanceData);
        setGroups(groupData || []);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  if (loading) return <Loading label="Opening your ledger" />;

  const totalOwe = Number(balances?.totalYouOwe || 0);
  const totalOwed = Number(balances?.totalOwedToYou || 0);
  const net = totalOwed - totalOwe;

  return (
    <section className="page-stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Dashboard</p>
          <h1>Your money map</h1>
          <p className="muted">
            A quick view of what you owe, what comes back to you, and where to
            settle next.
          </p>
        </div>
        <a className="button ghost" href="#/groups/new">
          Create group
        </a>
      </div>

      <ErrorMessage message={error} />

      <div className="stats-grid">
        <article className="stat-card">
          <span className="stat-icon positive">
            <ArrowUpRight size={20} />
          </span>
          <p>Owed to you</p>
          <strong>{currency.format(totalOwed)}</strong>
        </article>
        <article className="stat-card">
          <span className="stat-icon negative">
            <ArrowDownLeft size={20} />
          </span>
          <p>You owe</p>
          <strong>{currency.format(totalOwe)}</strong>
        </article>
        <article className="stat-card">
          <span className="stat-icon neutral">
            <Scale size={20} />
          </span>
          <p>Net position</p>
          <strong>{currency.format(net)}</strong>
        </article>
        <article className="stat-card">
          <span className="stat-icon neutral">
            <UsersRound size={20} />
          </span>
          <p>Groups</p>
          <strong>{groups.length}</strong>
        </article>
      </div>

      <div className="two-column">
        <section className="panel">
          <div className="panel-heading">
            <h2>Balance details</h2>
          </div>
          {balances?.balanceDetails?.length ? (
            <div className="balance-list">
              {balances.balanceDetails.map((item) => (
                <div className="balance-row" key={item.withUser}>
                  <span>{item.withUser}</span>
                  <strong className={Number(item.amount) >= 0 ? "green" : "red"}>
                    {currency.format(Number(item.amount))}
                  </strong>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">No open balances yet.</div>
          )}
        </section>

        <section className="panel">
          <div className="panel-heading">
            <h2>Recent groups</h2>
            <a href="#/groups">View all</a>
          </div>
          {groups.length ? (
            <div className="compact-list">
              {groups.slice(0, 4).map((group) => (
                <a className="compact-row" href={`#/groups/${group.id}`} key={group.id}>
                  <span>{group.name}</span>
                  <small>{group.members?.length || 0} members</small>
                </a>
              ))}
            </div>
          ) : (
            <div className="empty-state">Create your first group to begin.</div>
          )}
        </section>
      </div>
    </section>
  );
}
