import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import API from "../../services/api";
import AdminSidebar from "../../components/Admin/AdminSidebar";
import "./AdminDashboard.css";
import "./CommandCenter.css";

const numberFormat = new Intl.NumberFormat("en-US");

function formatNumber(value) {
  return numberFormat.format(Number(value) || 0);
}

function formatDateTime(value) {
  if (!value) return "Not generated yet";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Not generated yet";
  return date.toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function CommandCenter() {
  const [snapshot, setSnapshot] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadSnapshot = async () => {
    try {
      setLoading(true);
      setError("");
      const response = await API.get("/admin/command-center");
      setSnapshot(response.data);
    } catch (err) {
      setSnapshot(null);
      setError(err.response?.data?.message || "Command Center data is unavailable.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSnapshot();
  }, []);

  const metrics = snapshot?.metrics || {};
  const bookingTrend = snapshot?.bookingTrend || [];
  const maxTrend = useMemo(
    () => Math.max(...bookingTrend.map((item) => Number(item.totalBookings) || 0), 1),
    [bookingTrend],
  );

  const statCards = [
    {
      label: "Pending Bookings",
      value: metrics.pendingBookings,
      helper: "Awaiting admin decision",
      tone: "amber",
    },
    {
      label: "Active Resources",
      value: metrics.activeResources,
      helper: `${formatNumber(metrics.outOfServiceResources)} out of service`,
      tone: "green",
    },
    {
      label: "Open Tickets",
      value: (Number(metrics.openTickets) || 0) + (Number(metrics.inProgressTickets) || 0),
      helper: "Open and in progress",
      tone: "red",
    },
    {
      label: "Registered Users",
      value: metrics.totalUsers,
      helper: "All roles combined",
      tone: "blue",
    },
  ];

  return (
    <section className="admin-layout command-layout">
      <AdminSidebar />

      <div className="admin-shell command-shell">
        <header className="admin-head command-head">
          <div>
            <p className="admin-kicker">Innovation Feature</p>
            <h1>Campus Command Center</h1>
            <p className="admin-subtitle">
              A live operations view for booking demand, service risk, and campus readiness.
            </p>
          </div>
          <div className="command-head-actions">
            <span>{formatDateTime(snapshot?.generatedAt)}</span>
            <button type="button" className="btn btn-primary" onClick={loadSnapshot}>
              Refresh
            </button>
          </div>
        </header>

        {loading ? (
          <section className="command-state-panel">
            <div className="command-loader" />
            <p>Loading command snapshot...</p>
          </section>
        ) : error ? (
          <section className="command-state-panel command-error-panel">
            <h2>Command Center Offline</h2>
            <p>{error}</p>
            <button type="button" className="btn btn-primary" onClick={loadSnapshot}>
              Try Again
            </button>
          </section>
        ) : (
          <>
            <section className="command-stats" aria-label="Command Center metrics">
              {statCards.map((card) => (
                <article className={`command-stat-card tone-${card.tone}`} key={card.label}>
                  <span>{card.label}</span>
                  <strong>{formatNumber(card.value)}</strong>
                  <p>{card.helper}</p>
                </article>
              ))}
            </section>

            <section className="command-grid">
              <article className="command-panel command-panel-wide">
                <header className="command-panel-head">
                  <div>
                    <p>Booking Trend</p>
                    <h2>Seven Day Demand</h2>
                  </div>
                  <Link to="/admin/bookings">Open Queue</Link>
                </header>
                <div className="command-trend-chart">
                  {bookingTrend.map((item) => (
                    <div className="command-trend-col" key={item.date}>
                      <span>{formatNumber(item.totalBookings)}</span>
                      <div className="command-trend-track">
                        <div
                          className="command-trend-fill"
                          style={{ height: `${((Number(item.totalBookings) || 0) / maxTrend) * 100}%` }}
                        />
                      </div>
                      <strong>{item.label}</strong>
                    </div>
                  ))}
                </div>
              </article>

              <article className="command-panel">
                <header className="command-panel-head">
                  <div>
                    <p>Risk Radar</p>
                    <h2>Priority Alerts</h2>
                  </div>
                </header>
                <div className="command-alert-list">
                  {(snapshot?.riskAlerts || []).map((alert) => (
                    <section className={`command-alert severity-${alert.severity?.toLowerCase()}`} key={alert.title}>
                      <span>{alert.severity}</span>
                      <h3>{alert.title}</h3>
                      <p>{alert.message}</p>
                      <strong>{alert.action}</strong>
                    </section>
                  ))}
                </div>
              </article>
            </section>

            <section className="command-grid command-grid-secondary">
              <article className="command-panel">
                <header className="command-panel-head">
                  <div>
                    <p>Resource Demand</p>
                    <h2>Most Requested</h2>
                  </div>
                  <Link to="/admin/resources">Resources</Link>
                </header>
                <div className="command-demand-list">
                  {(snapshot?.resourceDemand || []).length === 0 ? (
                    <p className="command-empty">No booking demand recorded yet.</p>
                  ) : (
                    snapshot.resourceDemand.map((resource) => (
                      <section className="command-demand-item" key={resource.resourceName}>
                        <div>
                          <strong>{resource.resourceName}</strong>
                          <span>{resource.status}</span>
                        </div>
                        <div className="command-demand-meter">
                          <span style={{ width: `${Math.min(Number(resource.utilizationScore) || 0, 100)}%` }} />
                        </div>
                        <p>
                          {formatNumber(resource.totalBookings)} requests, {formatNumber(resource.pendingBookings)} pending
                        </p>
                      </section>
                    ))
                  )}
                </div>
              </article>

              <article className="command-panel">
                <header className="command-panel-head">
                  <div>
                    <p>Ticket SLA</p>
                    <h2>Watchlist</h2>
                  </div>
                  <Link to="/tickets/admin">Tickets</Link>
                </header>
                <div className="command-watchlist">
                  {(snapshot?.slaWatchlist || []).length === 0 ? (
                    <p className="command-empty">No active ticket risks right now.</p>
                  ) : (
                    snapshot.slaWatchlist.map((ticket) => (
                      <section className="command-ticket-row" key={ticket.ticketId}>
                        <div>
                          <strong>{ticket.title || `Ticket ${ticket.ticketId}`}</strong>
                          <span>{ticket.status} - {ticket.priority}</span>
                        </div>
                        <div className={ticket.breached ? "is-breached" : ""}>
                          {formatNumber(ticket.ageHours)}h / {formatNumber(ticket.targetHours)}h
                        </div>
                      </section>
                    ))
                  )}
                </div>
              </article>
            </section>
          </>
        )}
      </div>
    </section>
  );
}

export default CommandCenter;
