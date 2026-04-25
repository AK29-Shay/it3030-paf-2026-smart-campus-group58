const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export async function getCampusResources() {
  const response = await fetch(`${API_BASE_URL}/resources`);

  if (!response.ok) {
    throw new Error('Failed to load campus resources');
  }

  return response.json();
}
