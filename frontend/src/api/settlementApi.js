import { apiRequest } from "./apiClient";

export const settlementApi = {
  getSimplifiedDebts(groupId) {
    return apiRequest(`/api/settlement/group/${groupId}/simplify`);
  },

  settle(payload) {
    return apiRequest("/api/settlement/settle", {
      method: "POST",
      body: payload,
    });
  },

  getHistory(groupId) {
    return apiRequest(`/api/settlement/group/${groupId}/history`);
  },
};
