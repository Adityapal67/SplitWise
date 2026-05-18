import { useEffect, useRef, useState } from "react";
import { Plus, Search } from "lucide-react";
import { groupApi } from "../api/groupApi";
import ErrorMessage from "../components/ErrorMessage";
import Loading from "../components/Loading";

export default function Groups({ startWithCreate = false }) {
  const [groups, setGroups] = useState([]);
  const [form, setForm] = useState({ groupName: "", description: "" });
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const createRef = useRef(null);

  useEffect(() => {
    loadGroups();
  }, []);

  useEffect(() => {
    if (startWithCreate && createRef.current) {
      createRef.current.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }, [startWithCreate, loading]);

  async function loadGroups() {
    setLoading(true);
    setError("");
    try {
      setGroups((await groupApi.getMyGroups()) || []);
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

  async function createGroup(event) {
    event.preventDefault();
    setSaving(true);
    setError("");

    try {
      const created = await groupApi.createGroup(form);
      setGroups((current) => [created, ...current]);
      setForm({ groupName: "", description: "" });
      window.location.hash = `#/groups/${created.id}`;
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  const visibleGroups = groups.filter((group) =>
    `${group.name} ${group.description || ""}`
      .toLowerCase()
      .includes(query.toLowerCase())
  );

  if (loading) return <Loading label="Loading groups" />;

  return (
    <section className="page-stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Groups</p>
          <h1>Shared spaces</h1>
          <p className="muted">Create groups, open ledgers, and keep every split traceable.</p>
        </div>
      </div>

      <ErrorMessage message={error} />

      <form className="create-band" onSubmit={createGroup} ref={createRef}>
        <div>
          <h2>Create a group</h2>
          <p className="muted">Add members after the group is created.</p>
        </div>
        <input
          name="groupName"
          value={form.groupName}
          onChange={updateForm}
          placeholder="Goa trip"
          required
        />
        <input
          name="description"
          value={form.description}
          onChange={updateForm}
          placeholder="Weekend expense tracker"
        />
        <button className="button" type="submit" disabled={saving}>
          <Plus size={18} />
          <span>{saving ? "Creating" : "Create"}</span>
        </button>
      </form>

      <div className="toolbar">
        <div className="search-box">
          <Search size={18} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search groups"
          />
        </div>
      </div>

      {visibleGroups.length ? (
        <div className="group-grid">
          {visibleGroups.map((group) => (
            <a className="group-card" href={`#/groups/${group.id}`} key={group.id}>
              <div className="group-card-top">
                <span className="group-avatar">{group.name?.charAt(0)?.toUpperCase()}</span>
                <small>{group.members?.length || 0} members</small>
              </div>
              <h2>{group.name}</h2>
              <p>{group.description || "No description added yet."}</p>
              <span className="text-link">Open ledger</span>
            </a>
          ))}
        </div>
      ) : (
        <div className="empty-state">No groups found.</div>
      )}
    </section>
  );
}
