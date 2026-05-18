import { apiRequest } from "./apiClient";

export const expenseApi = {
  addExpense(payload) {
    return apiRequest("/api/expenses", {
      method: "POST",
      body: payload,
    });
  },

  getGroupExpenses(groupId) {
    return apiRequest(`/api/expenses/group/${groupId}`);
  },

  getMyBalances() {
    return apiRequest("/api/expenses/my-balances");
  },
};
