export default function Loading({ label = "Loading" }) {
  return (
    <div className="state-panel">
      <span className="loader" />
      <span>{label}</span>
    </div>
  );
}
