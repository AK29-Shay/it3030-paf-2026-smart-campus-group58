import { useAuth } from '../context/AuthContext.jsx';

function LoginPage() {
  const { login } = useAuth();

  return (
    <div className="auth-panel">
      <h1>Sign in</h1>
      <p>Use this placeholder login while authentication is being built.</p>
      <button type="button" onClick={() => login({ name: 'Demo User', role: 'STUDENT' })}>
        Continue as Demo User
      </button>
    </div>
  );
}

export default LoginPage;
