function StatusCard({ title, value, description }) {
  return (
    <section className="status-card">
      <p className="status-label">{title}</p>
      <strong>{value}</strong>
      <span>{description}</span>
    </section>
  );
}

export default StatusCard;
