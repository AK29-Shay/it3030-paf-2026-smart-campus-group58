import { Link, Route, Routes } from 'react-router-dom';
import DashboardPage from './pages/DashboardPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import AdminResourcePage from './pages/Resource/AdminResourcePage.jsx';
import ResourceList from './pages/Resource/ResourceList.jsx';
import ProtectedRoute from './routes/ProtectedRoute.jsx';

function App() {
  return (
    <div className="app-shell">
      <header className="topbar">
        <Link className="brand" to="/">Smart Campus</Link>
        <nav>
          <Link to="/">Dashboard</Link>
          <Link to="/resources">Resources</Link>
          <Link to="/admin/resources">Admin Resources</Link>
          <Link to="/login">Login</Link>
        </nav>
      </header>

      <main className="page-wrap">
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route path="/resources" element={<ResourceList />} />
          <Route path="/admin/resources" element={<AdminResourcePage />} />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
