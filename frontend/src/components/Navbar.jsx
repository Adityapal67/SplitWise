import {
  ArrowRightLeft,
  BarChart3,
  LogOut,
  PlusCircle,
  UsersRound,
} from "lucide-react";

const navItems = [
  { hash: "#/dashboard", label: "Dashboard", icon: BarChart3 },
  { hash: "#/groups", label: "Groups", icon: UsersRound },
];

export default function Navbar({ user, route, onLogout }) {
  return (
    <header className="topbar">
      <a className="brand" href="#/dashboard" aria-label="LedgerFlow home">
        <span className="brand-mark">
          <ArrowRightLeft size={19} />
        </span>
        <span>LedgerFlow</span>
      </a>

      <nav className="nav-links" aria-label="Main navigation">
        {navItems.map((item) => {
          const Icon = item.icon;
          const active = route.startsWith(item.hash.replace("#", ""));
          return (
            <a className={active ? "nav-link active" : "nav-link"} href={item.hash} key={item.hash}>
              <Icon size={17} />
              <span>{item.label}</span>
            </a>
          );
        })}
        <a className="nav-link primary-link" href="#/groups/new">
          <PlusCircle size={17} />
          <span>New Group</span>
        </a>
      </nav>

      <div className="account-pill">
        <span className="avatar">{user?.name?.charAt(0)?.toUpperCase() || "U"}</span>
        <span className="account-name">{user?.name || "User"}</span>
        <button className="icon-button" type="button" onClick={onLogout} aria-label="Logout">
          <LogOut size={17} />
        </button>
      </div>
    </header>
  );
}
