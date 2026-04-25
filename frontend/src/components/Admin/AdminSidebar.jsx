import { Link } from "react-router-dom";

function AdminSidebar() {
  return (
    <aside className="admin-sidebar">
      <Link to="/">Dashboard</Link>
      <Link to="/admin/resources">Resources</Link>
    </aside>
  );
}

export default AdminSidebar;
