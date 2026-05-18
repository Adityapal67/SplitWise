import { apiRequest } from "./apiClient";

export const groupApi = {
  createGroup(payload) {
    return apiRequest("/api/group", {
      method: "POST",
      body: payload,
    });
  },

  getMyGroups() {
    return apiRequest("/api/group/my");
  },

  getGroup(groupId) {
    return apiRequest(`/api/group/${groupId}`);
  },

  addMember(groupId, userId) {
    return apiRequest(`/api/group/${groupId}/members`, {
      method: "POST",
      body: { userId: Number(userId) },
    });
  },

  removeMember(groupId, userId) {
    return apiRequest(`/api/group/${groupId}/members/${userId}`, {
      method: "DELETE",
    });
  },
};
