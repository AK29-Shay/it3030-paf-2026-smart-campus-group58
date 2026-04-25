import StatusCard from '../components/StatusCard.jsx';

function DashboardPage() {
  return (
    <div className="dashboard">
      <div className="page-heading">
        <p>Operations Hub</p>
        <h1>Smart Campus Dashboard</h1>
      </div>

      <div className="status-grid">
        <StatusCard title="Resources" value="0" description="Ready for resource booking data" />
        <StatusCard title="Requests" value="0" description="Maintenance tickets will appear here" />
        <StatusCard title="Notifications" value="0" description="Campus alerts and updates" />
      </div>
    </div>
  );
}

export default DashboardPage;
