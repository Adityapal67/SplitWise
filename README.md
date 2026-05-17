# Splitwise Backend API 💸

A production-ready expense splitting REST API inspired by Splitwise,
built with Java and Spring Boot. Features JWT authentication, 3 split
types, and a debt simplification algorithm that minimizes settlement transactions.

🔗 **Live API:** https://splitwise-api.onrender.com
📖 **Swagger Docs:** https://splitwise-api.onrender.com/swagger-ui.html

---

## Tech Stack

![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-green?style=flat&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat&logo=mysql)
![JWT](https://img.shields.io/badge/JWT-Auth-purple?style=flat)
![Render](https://img.shields.io/badge/Deployed-Render-46E3B7?style=flat)

---

## Features

- 🔐 **JWT Authentication** — stateless auth with BCrypt password hashing
- 👥 **Group Management** — create groups, add/remove members
- 💰 **Expense Tracking** — 3 split types: Equal, Exact, Percentage
- 🧠 **Debt Simplification Algorithm** — greedy O(n log n) algorithm using
  priority queues that minimizes the number of settlement transactions
- 📊 **Balance Tracking** — real-time balance calculation per user
- ✅ **Settlement Recording** — mark debts as paid and track history

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Groups
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/groups` | Create a new group |
| GET | `/api/groups/my` | Get all groups you're in |
| GET | `/api/groups/{id}` | Get group details + members |
| POST | `/api/groups/{id}/members` | Add a member |
| DELETE | `/api/groups/{id}/members/{userId}` | Remove a member |

### Expenses
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/expenses` | Add an expense (EQUAL/EXACT/PERCENTAGE) |
| GET | `/api/expenses/group/{id}` | All expenses in a group |
| GET | `/api/expenses/my-balances` | Your net balance across all groups |

### Settlements
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/settlements/group/{id}/simplify` | Get minimum transactions to settle |
| POST | `/api/settlements/settle` | Record a payment |
| GET | `/api/settlements/group/{id}/history` | Settlement history |

---

## Database Schema

6 tables: `users` · `groups` · `group_members` · `expenses` · `expense_splits` · `settlements`

---

## The Debt Simplification Algorithm

The `/simplify` endpoint uses a greedy algorithm with two max-heaps
(one for creditors, one for debtors) to produce the minimum number of
transactions required to settle all debts in a group.

**Example:**
Without simplification: A→B ₹300, B→C ₹200, C→A ₹100 = 3 transactions
After simplification:   A→B ₹200, C→B ₹100 = 2 transactions

Time complexity: O(n log n) | Space: O(n)

---

## Running Locally

```bash
# Clone the repo
git clone https://github.com/yourusername/splitwise.git
cd splitwise

# Set up MySQL
mysql -u root -p
CREATE DATABASE splitwise_db;

# Configure application.properties with your DB credentials

# Run
mvn spring-boot:run

# API available at
http://localhost:8080/swagger-ui.html
```

---

## Author

**Aditya** — B.Tech CSE student passionate about backend systems
[LinkedIn](www.linkedin.com/in/aditya-pal-5143442a4) · [GitHub](https://github.com/Adityapal67)