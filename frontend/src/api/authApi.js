import { apiRequest } from "./apiClient";

export const authApi = {
  register(payload) {
    return apiRequest("/api/auth/register", {
      method: "POST",
      body: payload,
    });
  },

  login(payload) {
    return apiRequest("/api/auth/login", {
      method: "POST",
      body: payload,
    });
  },
};
