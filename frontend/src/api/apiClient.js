const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "https://ledgerflow-5zs2.onrender.com";

export const storageKeys = {
  token: "ledgerflow_token",
  user: "ledgerflow_user",
};

export function getToken() {
  return localStorage.getItem(storageKeys.token);
}

export function getStoredUser() {
  const value = localStorage.getItem(storageKeys.user);
  if (!value) return null;

  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

export function setSession(authResponse) {
  localStorage.setItem(storageKeys.token, authResponse.token);
  localStorage.setItem(
    storageKeys.user,
    JSON.stringify({
      email: authResponse.email,
      name: authResponse.name,
    })
  );
}

export function clearSession() {
  localStorage.removeItem(storageKeys.token);
  localStorage.removeItem(storageKeys.user);
}

async function parseResponse(response) {
  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }

  const text = await response.text();
  return text || null;
}

export async function apiRequest(path, options = {}) {
  const token = getToken();
  const headers = {
    Accept: "application/json",
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    body:
      options.body && typeof options.body !== "string"
        ? JSON.stringify(options.body)
        : options.body,
  });

  const data = await parseResponse(response);

  if (!response.ok) {
    const message =
      typeof data === "string"
        ? data
        : data?.error || data?.message || "Request failed";
    throw new Error(message);
  }

  return data;
}
